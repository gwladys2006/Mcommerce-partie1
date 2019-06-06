package com.ecommerce.microcommerce.web.controller;

import com.ecommerce.microcommerce.dao.ProductDao;
import com.ecommerce.microcommerce.model.Product;
import com.ecommerce.microcommerce.web.exceptions.ProduitIntrouvableException;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Api( description="API pour les opérations CRUD sur les produits")
@RestController
public class ProductController {

    @Autowired
    private ProductDao productDao;

    @ApiOperation(value = "Récupère tous les produits")
    @RequestMapping(value = "/Produits", method = RequestMethod.GET)
    public MappingJacksonValue listeProduits() {
        Iterable<Product> produits = productDao.findAll();
        SimpleBeanPropertyFilter monFiltre = SimpleBeanPropertyFilter.serializeAllExcept("prixAchat");
        FilterProvider listDeNosFiltres = new SimpleFilterProvider().addFilter("monFiltreDynamique", monFiltre);
        MappingJacksonValue produitsFiltres = new MappingJacksonValue(produits);
        produitsFiltres.setFilters(listDeNosFiltres);
        return produitsFiltres;
    }

    @ApiOperation(value = "Récupère un produit grâce à son ID")
    @GetMapping(value = "/Produits/{id}")
    public Product afficherUnProduit(@PathVariable int id) {
        Product produit = productDao.findById(id);

        if (produit == null) {
        	throw new ProduitIntrouvableException("Le produit avec l'id " + id + " est introuvable.");
        }

        return produit;
    }

    @ApiOperation(value = "Ajoute un produit")
    @PostMapping(value = "/Produits")
    public ResponseEntity<Void> ajouterProduit(@Valid @RequestBody Product product) {
        Product productAdded =  productDao.save(product);
        
        if (productAdded == null)
            return ResponseEntity.noContent().build();
        
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(productAdded.getId())
                .toUri();
        
        return ResponseEntity.created(location).build();
    }

    @ApiOperation(value = "Supprime un produit grâce à son ID")
    @DeleteMapping (value = "/Produits/{id}")
    public void supprimerProduit(@PathVariable int id) {
        productDao.delete(id);
    }

    @ApiOperation(value = "Modifie un produit")
    @PutMapping (value = "/Produits")
    public void updateProduit(@RequestBody Product product) {
        productDao.save(product);
    }

    //Pour les tests
    @GetMapping(value = "test/produits/{prix}")
    public List<Product> testeDeRequetes(@PathVariable int prix) {
        return productDao.chercherUnProduitCher(400);
    }
    
    @ApiOperation(value = "Calcule la marge de chaque produit")
    @GetMapping(value = "/AdminProduits")
    public List<String> calculerMargeProduit() {
    	List<String> list = new ArrayList<String>();
    	List<Product> products = productDao.findAll();
    	
    	for (Product product : products) {
    		int marge = product.getPrix() - product.getPrixAchat();
    		list.add(product + " : " + marge);
    	}
    	
    	return list;
    }

}
