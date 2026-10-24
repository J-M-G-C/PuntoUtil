package pe.edu.cibertec.puntoutil.dto;

import java.util.List;

public class VentaRequest {
    private List<DetalleVentaRequest> detalles;

    public List<DetalleVentaRequest> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleVentaRequest> detalles) { this.detalles = detalles; }
}