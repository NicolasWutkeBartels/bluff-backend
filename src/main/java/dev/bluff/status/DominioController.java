package dev.bluff.status;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dominios")
public class DominioController {

    private final StatusService statusService;

    public DominioController(StatusService statusService) {
        this.statusService = statusService;
    }

    @GetMapping
    public List<DominioResposta> listar(@RequestParam String dominio) {
        return statusService.listarPorDominio(dominio).stream()
                .map(DominioResposta::de)
                .toList();
    }
}
