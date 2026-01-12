package com.example.fitplannerserver;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/")
    public String ciao() {
        return "Ciao! Il progetto Spring Boot funziona!";
    }

//    @PostMapping
//    public String aggiungiProdotto(@RequestBody Prodotto nuovoProdotto) {
//        // @RequestBody dice a Spring: "Prendi il JSON che arriva e spalmalo dentro l'oggetto nuovoProdotto"
//
//        inventario.add(nuovoProdotto);
//        return "Prodotto aggiunto: " + nuovoProdotto.getNome();
//    }
}