package com.pulapirata.core;

import java.util.Map;
import java.util.HashMap;


import playn.core.Json;
import static playn.core.PlayN.log;
import playn.core.util.Callback;

import com.pulapirata.core.PetAttribute;

// A simple set of classes for character attributes
//
// This is a simple class holding and managing a list of attributes, which is
// basically the game state.
//
// - this takes attribute listing and handling off of the main update() loop
//
// - this also validates sets
//
// - after each "set" in the attribute, the attribute updates
// its state to a qualitative one based on set boundaries,
// eg, for alcool the qualitative state can be "coma", "vomiting",
// "dizzy" or "normal".
//
// - when updating the game sprite, we follow sprite update logic in
// update(), e.g., we check the status of all attributes and follow a table of
// priority to set the current sprite based on that.
//   - based on such priority we also determine which attributes should be
//   listed on the statusbar.
//   - determine also if an alert should be placed.
//   - each attribute can claim a certain priority level, and the policy to
//   decide between more than one w same priority can be handled externally,
//   eg, show alternating messages. But a final external table should order up each
//   attribute in case of conflicts.
//   - the state of the game is determined by the collection of attributes
//   - based on the attributes we determine which actions are allowed or not
//
public class PetAttributes {
  public static String JSON = "pet/jsons/atributos.json";

  private PetAttribute alcool_;
  public PetAttribute alcool() { return alcool_; }
  private PetAttribute fome_;
  public PetAttribute fome() { return fome_; }
  private PetAttribute humor_;
  public PetAttribute humor() { return humor_; }
  private PetAttribute sede_;
  public PetAttribute sede() { return sede_; }
  private PetAttribute social_;
  public PetAttribute social() { return social_; }
  private PetAttribute higiene_;
  public PetAttribute higiene() { return higiene_; }
  private PetAttribute estudo_;
  public PetAttribute estudo() { return estudo_; }
  private PetAttribute saude_;
  public PetAttribute saude() { return saude_; }
  private PetAttribute disciplina_;
  public PetAttribute disciplina() { return disciplina_; }

  public Map<String, PetAttribute> m = new HashMap<String, PetAttribute>();

  // map by name
  void mapAttrib(PetAttribute att) {
    m.put(att.name(), att);
  }

  public PetAttributes(double beatsCoelhoHora) {
    // defalt values. values in the json will take precedence if available
    alcool_   = new PetAttribute("Alcool");
    fome_     = new PetAttribute("Fome");
    humor_    = new PetAttribute("Humor");
    sede_     = new PetAttribute("Sede");
    social_   = new PetAttribute("Social");
    higiene_  = new PetAttribute("Higiene");
    estudo_   = new PetAttribute("Estudo");
      //? por dia a partir da matricula (colocar um valor inicial depois da matricula mudar)
    saude_ = new PetAttribute("Saude");
      //? por idade (em dias?)
    disciplina_ = new PetAttribute("Disciplina");

    mapAttrib(alcool());
    mapAttrib(fome());
    mapAttrib(humor());
    mapAttrib(sede());
    mapAttrib(social());
    mapAttrib(higiene());
    mapAttrib(estudo());
    mapAttrib(saude());
    mapAttrib(disciplina());
    // for i in attribute_list, set

    // TODO populateFromJson();
  }

  public void print() {
    for (String key : m.keySet()) {
      m.get(key).print();
    }
  }

//  public void populateFromJson() {
//    petJson.parseJson("pet/jsons/atributos.json","fome_");
//    fome().set(PetJson.readJson("pet/jsons/atributos.json","fome").fome());
//  }

//  TODO needed? private ArrayList<Attribute> attribute =
//        new ArrayList<Attribute>(Arrays.asList(
//        alcool_
//        ));
}