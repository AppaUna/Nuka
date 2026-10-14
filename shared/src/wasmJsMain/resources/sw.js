// Nuka Service Worker
// Версия повышается при каждой правке — старый кэш удаляется автоматически.
const CACHE_NAME = 'nuka-v3';

// App shell — минимальный набор файлов, которые кэшируем сразу при установке.
const APP_SHELL = [
    '/',
    '/index.html',
    '/manifest.json'
];

// Файлы, которые меняются при каждой сборке — всегда идём в сеть, кэш обновляем в фоне.
// Это .wasm и nuka.js — они хешированы по содержимому, но nuka.js перезаписывается.
const NETWORK_FIRST = [
    '/nuka.js',
    '/nuka.wasm'
];

// Установка — кэшируем app shell
self.addEventListener('install', (event) => {
    event.waitUntil(
        caches.open(CACHE_NAME).then((cache) => cache.addAll(APP_SHELL))
    );
    self.skipWaiting();
});

// Активация — удаляем все старые кэши
self.addEventListener('activate', (event) => {
    event.waitUntil(
        caches.keys().then((keys) =>
            Promise.all(
                keys.filter((key) => key !== CACHE_NAME).map((key) => caches.delete(key))
            )
        )
    );
    self.clients.claim();
});

// Fetch — стратегия зависит от типа ресурса
self.addEventListener('fetch', (event) => {
    const url = new URL(event.request.url);

    // Пропускаем всё, что не наш origin (например, Supabase Storage с фото)
    if (url.origin !== self.location.origin) {
        return;
    }

    // Пропускаем API-запросы к Supabase — их кэшировать нельзя
    if (url.pathname.startsWith('/api/')) {
        return;
    }

    // 1. Network-first для nuka.js и *.wasm — всегда свежая версия
    const isNetworkFirst = NETWORK_FIRST.some(path => url.pathname.endsWith(path))
        || url.pathname.endsWith('.wasm');

    if (isNetworkFirst) {
        event.respondWith(
            fetch(event.request)
                .then((response) => {
                    // Клонируем и обновляем кэш
                    if (response.ok) {
                        const responseClone = response.clone();
                        caches.open(CACHE_NAME).then((cache) => {
                            cache.put(event.request, responseClone);
                        });
                    }
                    return response;
                })
                .catch(() => caches.match(event.request))
        );
        return;
    }

    // 2. Cache-first для composeResources (шрифты, иконки) — они не меняются часто
    if (url.pathname.includes('/composeResources/')) {
        event.respondWith(
            caches.match(event.request).then((response) => {
                if (response) return response;
                return fetch(event.request).then((fetchResponse) => {
                    if (fetchResponse.ok) {
                        const responseClone = fetchResponse.clone();
                        caches.open(CACHE_NAME).then((cache) => {
                            cache.put(event.request, responseClone);
                        });
                    }
                    return fetchResponse;
                });
            })
        );
        return;
    }

    // 3. Cache-first для всего остального (HTML, манифест, иконки)
    event.respondWith(
        caches.match(event.request).then((response) => response || fetch(event.request))
    );
});