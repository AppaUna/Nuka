export function requestPushPermission() {
    if (typeof window.requestPushPermission === 'function') {
        window.requestPushPermission();
    } else {
        console.error("JS: window.requestPushPermission не найдена");
    }
}