// VERSION: 2026-11-06-js-2
const CACHE_NAME = 'nuka-js-v6';
const APP_SHELL = [
    '/js/',
    '/js/index.html',
    '/js/manifest.json'
];
const NETWORK_FIRST = [
    '/js/shared.js',
    '/js/skiko.wasm'
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

    if (url.origin !== self.location.origin) return;
    if (url.pathname.startsWith('/api/')) return;

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

    event.respondWith(
        caches.match(event.request).then((response) => response || fetch(event.request))
    );
});

self.addEventListener('message', (event) => {
    if (event.data && event.data.type === 'SKIP_WAITING') {
        self.skipWaiting();
    }
});

// ==========================================
// PUSH УВЕДОМЛЕНИЯ
// ==========================================
self.addEventListener('push', function(event) {
    if (!event.data) return;

    try {
        const data = event.data.json();
        const title = data.title || 'Nuka';
        const options = {
            body: data.body || 'У вас новое уведомление',
            icon: '/js/icons/apple-touch-icon.png',
            badge: '/js/icons/apple-touch-icon.png',
            data: { url: data.url || '/' }
        };

        event.waitUntil(
            self.registration.showNotification(title, options)
        );
    } catch (e) {
        console.error('Push event error:', e);
    }
});

self.addEventListener('notificationclick', function(event) {
    event.notification.close();
    const urlToOpen = event.notification.data.url || '/js/';

    event.waitUntil(
        clients.matchAll({ type: 'window', includeUncontrolled: true }).then(function(clientList) {
            for (var i = 0; i < clientList.length; i++) {
                var client = clientList[i];
                if (client.url === urlToOpen && 'focus' in client) {
                    return client.focus();
                }
            }
            if (clients.openWindow) {
                return clients.openWindow(urlToOpen);
            }
        })
    );
});