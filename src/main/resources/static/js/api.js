const API_BASE_URL = "";

function getToken() {
    return localStorage.getItem("token");
}

function getAuthHeaders() {
    const token = getToken();

    return {
        "Content-Type": "application/json",
        "Authorization": "Bearer " + token
    };
}

async function apiGet(endpoint) {
    console.log("GET:", endpoint);

    const response = await fetch(API_BASE_URL + endpoint, {
        method: "GET",
        headers: getAuthHeaders()
    });

    if (response.status === 401) {
        throw new Error("401 - Token inválido o no enviado");
    }

    if (response.status === 403) {
        throw new Error("403 - No tienes permisos para acceder a " + endpoint);
    }

    if (!response.ok) {
        throw new Error("Error consultando " + endpoint + ". Estado: " + response.status);
    }

    return await response.json();
}

async function apiPostPublic(endpoint, body) {
    console.log("POST PUBLIC:", endpoint);

    const response = await fetch(API_BASE_URL + endpoint, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(body)
    });

    if (!response.ok) {
        throw new Error("Error en la petición pública " + endpoint + ". Estado: " + response.status);
    }

    return await response.json();
}

async function apiPostProtected(endpoint, body) {
    console.log("POST PROTECTED:", endpoint);

    const response = await fetch(API_BASE_URL + endpoint, {
        method: "POST",
        headers: getAuthHeaders(),
        body: JSON.stringify(body)
    });

    if (response.status === 401) {
        throw new Error("401 - Token inválido o no enviado");
    }

    if (response.status === 403) {
        throw new Error("403 - No tienes permisos para acceder a " + endpoint);
    }

    if (!response.ok) {
        throw new Error("Error en la petición protegida " + endpoint + ". Estado: " + response.status);
    }

    return await response.json();
}

function cerrarSesion() {
    localStorage.removeItem("token");
    localStorage.removeItem("email");
    localStorage.removeItem("rol");

    window.location.href = "login.html";
}