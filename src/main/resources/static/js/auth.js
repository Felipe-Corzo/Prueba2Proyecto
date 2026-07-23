document.addEventListener("DOMContentLoaded", () => {
    const loginForm = document.getElementById("loginForm");
    const loginMessage = document.getElementById("loginMessage");

    if (!loginForm) {
        return;
    }

    loginForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        const email = document.getElementById("email").value.trim();
        const password = document.getElementById("password").value.trim();

        loginMessage.textContent = "Validando credenciales...";

        try {
            const data = await apiPostPublic("/auth/login", {
                email: email,
                password: password
            });

            localStorage.setItem("token", data.token);
            localStorage.setItem("email", data.email);
            localStorage.setItem("rol", data.rol);

            window.location.href = "dashboard.html";
        } catch (error) {
            console.error(error);
            loginMessage.textContent = "Email o contraseña incorrectos.";
        }
    });
});