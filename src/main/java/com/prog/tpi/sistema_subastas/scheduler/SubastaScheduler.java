package com.prog.tpi.sistema_subastas.scheduler;

import com.prog.tpi.sistema_subastas.models.*;
import com.prog.tpi.sistema_subastas.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
public class SubastaScheduler {

    private static final Logger log = LoggerFactory.getLogger(SubastaScheduler.class);

    private final SubastaRepository subastaRepository;
    private final PujaRepository pujaRepository;
    private final HistorialEstadoRepository historialEstadoRepository;
    private final NotificacionRepository notificacionRepository;

    public SubastaScheduler(SubastaRepository subastaRepository,
                            PujaRepository pujaRepository,
                            HistorialEstadoRepository historialEstadoRepository,
                            NotificacionRepository notificacionRepository) {
        this.subastaRepository = subastaRepository;
        this.pujaRepository = pujaRepository;
        this.historialEstadoRepository = historialEstadoRepository;
        this.notificacionRepository = notificacionRepository;
    }

    @Scheduled(fixedDelay = 30_000)
    @Transactional
    public void procesarTransiciones() {
        Instant ahora = Instant.now();

        activarSubastas(ahora);
        cerrarSubastas(ahora);
    }

    // PUBLICADA → ACTIVA cuando fechaInicio <= ahora
    private void activarSubastas(Instant ahora) {
        List<Subasta> paraActivar = subastaRepository
                .findByEstadoAndFechaInicioBefore(EstadoSubasta.PUBLICADA, ahora);

        for (Subasta subasta : paraActivar) {
            EstadoSubasta estadoAnterior = subasta.getEstado();
            subasta.setEstado(EstadoSubasta.ACTIVA);
            subastaRepository.save(subasta);

            registrarHistorial(subasta, estadoAnterior, EstadoSubasta.ACTIVA, "Activación automática por scheduler");
            log.info("Subasta {} activada automáticamente", subasta.getId());
        }
    }

    // ACTIVA → ADJUDICADA (con ganador) o FINALIZADA (sin pujas) cuando fechaCierre <= ahora
    private void cerrarSubastas(Instant ahora) {
        List<Subasta> paraCerrar = subastaRepository
                .findByEstadoAndFechaCierreBefore(EstadoSubasta.ACTIVA, ahora);

        for (Subasta subasta : paraCerrar) {
            EstadoSubasta estadoAnterior = subasta.getEstado();

            Optional<Puja> mejorPuja = pujaRepository.findTopBySubastaOrderByMontoOfertadoDesc(subasta);

            if (mejorPuja.isPresent()) {
                // Hay ganador → ADJUDICADA
                Usuario ganador = mejorPuja.get().getUsuarioOferente();
                subasta.setGanadorActual(ganador);
                subasta.setEstado(EstadoSubasta.ADJUDICADA);
                subastaRepository.save(subasta);

                registrarHistorial(subasta, estadoAnterior, EstadoSubasta.ADJUDICADA,
                        "Subasta cerrada con ganador: " + ganador.getUsernameEmail());

                // Notificar al ganador
                enviarNotificacion(ganador,
                        "¡Felicitaciones! Ganaste la subasta \"" + subasta.getProducto().getNombre()
                                + "\" con una oferta de $" + subasta.getMontoActual() + ".");

                // Notificar al vendedor
                enviarNotificacion(subasta.getVendedor(),
                        "Tu subasta \"" + subasta.getProducto().getNombre()
                                + "\" finalizó. Ganador: " + ganador.getUsernameEmail()
                                + " con $" + subasta.getMontoActual() + ".");

                log.info("Subasta {} adjudicada a {}", subasta.getId(), ganador.getUsernameEmail());

            } else {
                // Sin pujas → FINALIZADA
                subasta.setEstado(EstadoSubasta.FINALIZADA);
                subastaRepository.save(subasta);

                registrarHistorial(subasta, estadoAnterior, EstadoSubasta.FINALIZADA,
                        "Subasta cerrada sin ofertas.");

                // Notificar al vendedor
                enviarNotificacion(subasta.getVendedor(),
                        "Tu subasta \"" + subasta.getProducto().getNombre()
                                + "\" finalizó sin recibir ofertas.");

                log.info("Subasta {} finalizada sin pujas", subasta.getId());
            }
        }
    }

    private void registrarHistorial(Subasta subasta, EstadoSubasta anterior,
                                    EstadoSubasta nuevo, String motivo) {
        HistorialEstado historial = HistorialEstado.builder()
                .subasta(subasta)
                .estadoAnterior(anterior)
                .estadoNuevo(nuevo)
                .fecha(Instant.now())
                .motivo(motivo)
                .usuarioResponsable(null) // transición automática
                .build();
        historialEstadoRepository.save(historial);
    }

    private void enviarNotificacion(Usuario destinatario, String mensaje) {
        Notificacion notificacion = Notificacion.builder()
                .usuarioDestino(destinatario)
                .mensaje(mensaje)
                .leido(false)
                .fecha(Instant.now())
                .build();
        notificacionRepository.save(notificacion);
    }
}
