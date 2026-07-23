document.addEventListener("DOMContentLoaded", () => {
    protegerDashboard();
    configurarMenu();
    configurarLogout();

    cargarDashboard();

    const refreshBtn = document.getElementById("refreshBtn");

    if (refreshBtn) {
        refreshBtn.addEventListener("click", cargarDashboard);
    }
});

function protegerDashboard() {
    const token = localStorage.getItem("token");

    if (!token) {
        window.location.href = "login.html";
    }
}

function configurarLogout() {
    const logoutBtn = document.getElementById("logoutBtn");

    if (logoutBtn) {
        logoutBtn.addEventListener("click", cerrarSesion);
    }
}

function configurarMenu() {
    const menuItems = document.querySelectorAll(".menu-item");
    const sections = document.querySelectorAll(".content-section");

    menuItems.forEach(item => {
        item.addEventListener("click", () => {
            menuItems.forEach(menu => menu.classList.remove("active"));
            sections.forEach(section => section.classList.remove("active-section"));

            item.classList.add("active");

            const sectionId = item.getAttribute("data-section");
            document.getElementById(sectionId).classList.add("active-section");

            if (sectionId === "bodegasSection") {
                cargarBodegas();
            }

            if (sectionId === "productosSection") {
                cargarProductos();
            }

            if (sectionId === "movimientosSection") {
                cargarMovimientos();
            }

            if (sectionId === "auditoriaSection") {
                cargarAuditorias();
            }
        });
    });
}

async function cargarDashboard() {
    try {
        const bodegas = await apiGet("/bodegas");
        const productos = await apiGet("/productos");
        const stockBajo = await apiGet("/productos/stock-bajo");
        const movimientos = await apiGet("/movimientos");

        let reporte = {
            stockPorBodega: [],
            productosMasMovidos: []
        };

        try {
            reporte = await apiGet("/reportes/resumen");
        } catch (errorReporte) {
            console.warn("No se pudo cargar /reportes/resumen:", errorReporte.message);
        }

        document.getElementById("totalBodegas").textContent = bodegas.length;
        document.getElementById("totalProductos").textContent = productos.length;
        document.getElementById("stockBajo").textContent = stockBajo.length;
        document.getElementById("movimientosMes").textContent = contarMovimientosDelMes(movimientos);

        renderizarStockPorBodega(reporte.stockPorBodega || []);
        renderizarTopProductos(reporte.productosMasMovidos || []);

    } catch (error) {
        console.error("Error cargando dashboard:", error.message);

        if (error.message.includes("401")) {
            localStorage.removeItem("token");
            localStorage.removeItem("email");
            localStorage.removeItem("rol");
            alert("La sesión expiró o el token no es válido. Inicia sesión nuevamente.");
            window.location.href = "login.html";
            return;
        }

        if (error.message.includes("403")) {
            alert("No tienes permisos para cargar todos los datos del dashboard.");
            return;
        }

        alert("No se pudo cargar el dashboard. Revisa la consola del navegador.");
    }
}

function contarMovimientosDelMes(movimientos) {
    const fechaActual = new Date();
    const mesActual = fechaActual.getMonth();
    const anioActual = fechaActual.getFullYear();

    return movimientos.filter(movimiento => {
        if (!movimiento.fecha) {
            return false;
        }

        const fechaMovimiento = new Date(movimiento.fecha);

        return fechaMovimiento.getMonth() === mesActual &&
               fechaMovimiento.getFullYear() === anioActual;
    }).length;
}

function renderizarStockPorBodega(stockPorBodega) {
    const container = document.getElementById("stockChart");
    container.innerHTML = "";

    if (stockPorBodega.length === 0) {
        container.innerHTML = "<p>No hay inventario registrado.</p>";
        return;
    }

    const maxStock = Math.max(...stockPorBodega.map(item => item.stockTotal));

    stockPorBodega.forEach(item => {
        const percentage = maxStock === 0 ? 0 : (item.stockTotal / maxStock) * 100;

        const row = document.createElement("div");
        row.classList.add("bar-row");

        row.innerHTML = `
            <div class="bar-info">
                <span>${item.bodega}</span>
                <span>${item.stockTotal}</span>
            </div>
            <div class="bar-track">
                <div class="bar-fill" style="width: ${percentage}%"></div>
            </div>
        `;

        container.appendChild(row);
    });
}

function renderizarTopProductos(productosMasMovidos) {
    const container = document.getElementById("topProductosList");
    container.innerHTML = "";

    if (productosMasMovidos.length === 0) {
        container.innerHTML = "<div class='top-item'>No hay movimientos registrados.</div>";
        return;
    }

    const topCinco = productosMasMovidos.slice(0, 5);

    topCinco.forEach(producto => {
        const item = document.createElement("div");
        item.classList.add("top-item");

        item.innerHTML = `
            <div class="product-icon">▤</div>
            <div>
                <div class="product-name">${producto.producto}</div>
                <div class="product-sku">SKU-${producto.productoId}</div>
            </div>
            <div class="move-count">${producto.cantidadMovida}</div>
        `;

        container.appendChild(item);
    });
}

async function cargarBodegas() {
    try {
        const bodegas = await apiGet("/bodegas");
        const tbody = document.getElementById("bodegasTable");

        tbody.innerHTML = "";

        bodegas.forEach(bodega => {
            const row = document.createElement("tr");

            row.innerHTML = `
                <td>${bodega.id}</td>
                <td>${bodega.nombre}</td>
                <td>${bodega.ubicacion}</td>
                <td>${bodega.capacidad}</td>
                <td>${bodega.encargado}</td>
            `;

            tbody.appendChild(row);
        });
    } catch (error) {
        console.error(error);
    }
}

async function cargarProductos() {
    try {
        const productos = await apiGet("/productos");
        const tbody = document.getElementById("productosTable");

        tbody.innerHTML = "";

        productos.forEach(producto => {
            const row = document.createElement("tr");

            row.innerHTML = `
                <td>${producto.id}</td>
                <td>${producto.nombre}</td>
                <td>${producto.categoria}</td>
                <td>${producto.stock}</td>
                <td>$${Number(producto.precio).toLocaleString("es-CO")}</td>
            `;

            tbody.appendChild(row);
        });
    } catch (error) {
        console.error(error);
    }
}

async function cargarMovimientos() {
    try {
        const movimientos = await apiGet("/movimientos");
        const tbody = document.getElementById("movimientosTable");

        tbody.innerHTML = "";

        movimientos.forEach(movimiento => {
            const row = document.createElement("tr");

            const usuario = movimiento.usuarioResponsable
                ? movimiento.usuarioResponsable.email
                : "N/A";

            const origen = movimiento.bodegaOrigen
                ? movimiento.bodegaOrigen.nombre
                : "-";

            const destino = movimiento.bodegaDestino
                ? movimiento.bodegaDestino.nombre
                : "-";

            row.innerHTML = `
                <td>${movimiento.id}</td>
                <td>${formatearFecha(movimiento.fecha)}</td>
                <td>${movimiento.tipoMovimiento}</td>
                <td>${usuario}</td>
                <td>${origen}</td>
                <td>${destino}</td>
            `;

            tbody.appendChild(row);
        });
    } catch (error) {
        console.error(error);
    }
}

async function cargarAuditorias() {
    try {
        const auditorias = await apiGet("/auditorias");
        const tbody = document.getElementById("auditoriasTable");

        tbody.innerHTML = "";

        auditorias.forEach(auditoria => {
            const row = document.createElement("tr");

            row.innerHTML = `
                <td>${auditoria.id}</td>
                <td>${auditoria.operacion}</td>
                <td>${auditoria.usuario}</td>
                <td>${auditoria.entidadAfectada}</td>
                <td>${formatearFecha(auditoria.fechaHora)}</td>
            `;

            tbody.appendChild(row);
        });
    } catch (error) {
        console.error(error);
        alert("No se pudieron cargar auditorías. Recuerda que solo ADMIN puede verlas.");
    }
}

function formatearFecha(fecha) {
    if (!fecha) {
        return "-";
    }

    return new Date(fecha).toLocaleString("es-CO");
}