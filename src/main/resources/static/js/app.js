document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('resenaForm');
    if (!form) return;

    form.addEventListener('submit', async function (e) {
        e.preventDefault();
        const action = form.getAttribute('action');
        const data = new FormData(form);
        const payload = Object.fromEntries(data.entries());

        try {
            const res = await fetch(action, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            const msg = document.getElementById('resenaMessage');
            if (res.ok) {
                msg.style.display = 'block';
                msg.textContent = 'Reseña enviada correctamente. Recargando...';
                setTimeout(() => location.reload(), 1200);
            } else if (res.status === 401) {
                msg.style.display = 'block';
                msg.textContent = 'Debes iniciar sesión para escribir una reseña.';
            } else {
                const text = await res.text();
                msg.style.display = 'block';
                msg.textContent = 'Error: ' + text;
            }
        } catch (err) {
            console.error(err);
        }
    });
});
