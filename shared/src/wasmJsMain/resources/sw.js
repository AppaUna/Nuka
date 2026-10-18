// VERSION: 2026-10-18-1
const CACHE_NAME = 'nuka-v6';
const APP_SHELL = [
    '/',
    '/index.html',
    '/manifest.json'
];
const NETWORK_FIRST = [
    '/nuka.js',
    '/nuka.wasm'
];

self.addEventListener('install', (event) => {
    event.waitUntil(
        caches.open(CACHE_NAME).then((cache) => cache.addAll(APP_SHELL))
    );
    self.skipWaiting();
});

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

self.addEventListener('fetch', (event) => {
    const url = new URL(event.request.url);

    // Пропускаем API и внешние ресурсы
    if (url.origin !== self.location.origin) return;
    if (url.pathname.startsWith('/api/')) return;

    // Network-first для nuka.js и *.wasm
    const isNetworkFirst = NETWORK_FIRST.some(path => url.pathname.endsWith(path))
        || url.pathname.endsWith('.wasm');

    if (isNetworkFirst) {
        event.respondWith(
            fetch(event.request)
                .then((response) => {
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

    // Cache-first для composeResources
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

    // Cache-first для остального (HTML, manifest, icons)
    event.respondWith(
        caches.match(event.request).then((response) => response || fetch(event.request))
    );
});

// Обработка сообщения для активации
self.addEventListener('message', (event) => {
    if (event.data && event.data.type === 'SKIP_WAITING') {
        self.skipWaiting();
    }
});