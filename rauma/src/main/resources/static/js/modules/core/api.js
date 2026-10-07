/* ==========================================================================
   MODULE: SMART API CALLER (Spring Boot :8080 <-> Apache PHP fallback)
   ========================================================================== */

/* ==========================================================================
   SMART API CALLER (Spring Boot :8080 -> Fallback to Apache PHP API)
   ========================================================================== */
async function callApi(endpoint, options = {}) {
    // 1. Try Java Spring Boot (localhost:8080) with 1s timeout
    try {
        const controller = new AbortController();
        const timer = setTimeout(() => controller.abort(), 1000);
        const response = await fetch(`http://localhost:8080/api/v1${endpoint}`, {
            ...options,
            headers: {
                'Content-Type': 'application/json',
                ...(currentUser.token ? { 'Authorization': `Bearer ${currentUser.token}` } : {}),
                ...(options.headers || {})
            },
            signal: controller.signal
        });
        clearTimeout(timer);
        if (response.ok || response.status === 400 || response.status === 409) {
            return response;
        }
    } catch (err) {
        // Fallback to PHP on Apache
    }

    // 2. Fallback: PHP API on Apache (api.php)
    const [pathPart, queryPart] = endpoint.split('?');
    const parts = pathPart.replace(/^\//, '').split('/').filter(Boolean);
    const resource = parts[0] || '';
    let phpUrl = `api.php?resource=${encodeURIComponent(resource)}`;

    if (parts[1] && isNaN(parts[1])) {
        phpUrl += `&action=${encodeURIComponent(parts[1])}`;
    } else if (parts[1] && !isNaN(parts[1])) {
        phpUrl += `&id=${parts[1]}`;
        if (parts[2]) {
            phpUrl += `&action=${encodeURIComponent(parts[2])}`;
        }
    }

    if (queryPart) {
        phpUrl += `&${queryPart}`;
    }

    return await fetch(phpUrl, {
        ...options,
        headers: {
            'Content-Type': 'application/json',
            ...(options.headers || {})
        }
    });
}

