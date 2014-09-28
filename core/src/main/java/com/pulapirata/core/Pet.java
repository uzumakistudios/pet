package com.pulapirata.core;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Arrays;
import java.util.Random;
import java.applet.Applet;
import java.applet.AudioClip;
import java.net.URL;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.*;
import static playn.core.PlayN.*;

import playn.core.Game;
import playn.core.GroupLayer;
import playn.core.Image;
import playn.core.ImageLayer;
import playn.core.Pointer;
import playn.core.CanvasImage;
import playn.core.util.Clock;
import playn.core.PlayN;
import playn.core.Font;
import playn.core.Sound;
import playn.core.util.Callback;

import com.pulapirata.core.Aviso;
import com.pulapirata.core.PetAttributes;
import com.pulapirata.core.utils.PetAttributesLoader;
import com.pulapirata.core.sprites.Pingo;
import com.pulapirata.core.sprites.PingoMorto;
import com.pulapirata.core.sprites.PingoVomitando;
import com.pulapirata.core.sprites.PingoBebado;
import com.pulapirata.core.sprites.PingoComa;
import com.pulapirata.core.sprites.PingoBebendoAgua;
import com.pulapirata.core.sprites.PingoBebendoLeite;
import com.pulapirata.core.sprites.PingoComendoSopaBacon;
import com.pulapirata.core.sprites.PingoComendoSopaCenoura;
import com.pulapirata.core.sprites.PingoPiscando;
import com.pulapirata.core.sprites.PingoDormindo;
import com.pulapirata.core.sprites.PingoChorando;
import com.pulapirata.core.sprites.PingoTriste;

// TODO: we need a generic sprite class; or the layer could automatically update
// them

import react.Function;
import react.UnitSlot;
import react.Slot;

import tripleplay.ui.Element;
import tripleplay.ui.Elements;
import tripleplay.ui.Selector;
import tripleplay.ui.Background;
import tripleplay.ui.Button;
import tripleplay.ui.Icon;
import tripleplay.ui.Icons;
import tripleplay.ui.ToggleButton;
import tripleplay.ui.Label;
import tripleplay.ui.Group;
import tripleplay.ui.SizableGroup;
import tripleplay.ui.Interface;
import tripleplay.ui.Root;
import tripleplay.ui.SimpleStyles;
import tripleplay.ui.Style;
import tripleplay.ui.Styles;
import tripleplay.ui.Stylesheet;
import tripleplay.ui.layout.TableLayout;
import tripleplay.ui.layout.AbsoluteLayout;
import tripleplay.ui.layout.AxisLayout;
import tripleplay.util.Randoms;
import static tripleplay.ui.layout.TableLayout.COL;


public class Pet extends Game.Default {
  /*===============================================================================*/
  /* Data                                                                          */
  /*===============================================================================*/

  /*-------------------------------------------------------------------------------*/
  /* Status info shown on top */

  protected  static final String STAT_ALERT_1 =
    "Pingo recebeu convite para ir a um aniversario de um colega na escola.";
  protected  static final String STAT_FILLER_1 = "Idade: %d%s\n Sede: %d/%d\n";
  protected  static final String STAT_FILLER_2 = "\nFome: %d/%d\n Alcool: %d/%d";

  /*-------------------------------------------------------------------------------*/
  /* Sounds */

  private Sound somArroto_ = assets().getSound("pet/sprites/arroto_01");
  private Sound somSoluco_ = assets().getSound("pet/sprites/soluco_01");

  /*-------------------------------------------------------------------------------*/

  // FIXME graphics.width() is weird in html, not respecting #playn-root
  // properties.

  public int width()  { return 480; }
  public int height() { return 800; }


  /*-------------------------------------------------------------------------------*/
  /* Time data */

  public static final int UPDATE_RATE = 100; // ms
   // the following is not static so that we can dynamically speedup the game if desired
  private int beatsCoelhoDia_ = 600; // beats por 1 coelho dia.
  private double beatsCoelhoHora_ = (double)beatsCoelhoDia_/24.f;
  private double beatsCoelhoSegundo_ = (double)beatsCoelhoDia_/(24.*60.*60.);
  public int idadeCoelhoHoras() { return (int)((double)beat_ / ((double)beatsCoelhoDia_/24.)); }
  public int idadeCoelhoDias() { return beat_ / beatsCoelhoDia_; }

  /*-------------------------------------------------------------------------------*/
  /* Pet attributes & info */

  protected PetAttributes a = new PetAttributes(beatsCoelhoHora_);
  private boolean attributesLoaded = false;
  private boolean printIniDbg = true;

  private boolean dormir_ = false;
  private int diaProibidoBeber_ = 0;
  /*-------------------------------------------------------------------------------*/
  /* Misc variables */

  private int r_; // excluir depois

  protected final Clock.Source clock_ = new Clock.Source(UPDATE_RATE);

  /*-------------------------------------------------------------------------------*/
  /* Layers, groups & associated resources */

  private ImageLayer bgLayer_ = null;
  private Image bgImageDay_, bgImageNight_;
  private GroupLayer layer_;
  protected Group mainStat_;
  private Group rightStatbarGroup_;
  private TableLayout rightPartLayout_;
  private Interface iface_, statbarIface_;
  private Image exclamacao_;
  private Stylesheet petSheet_;

  //--------------------------------------------------------------------------------
  public Pet() {
    super(UPDATE_RATE);
  }

  //--------------------------------------------------------------------------------
  private void makeStatusbar() {
    // create and add the status title layer using drawings for faster loading
    CanvasImage bgtile = graphics().createImage(480, 119);
    bgtile.canvas().setFillColor(0xFFFFFFFF);
    bgtile.canvas().fillRect(0, 0, 480, 119);
    bgtile.canvas().setFillColor(0xFF333366);
    bgtile.canvas().fillRect(4, 4, 472, 112);
    //Fonte
    //Font font = graphics().createFont("earthboundzero", Font.Style.PLAIN, 18);
    ImageLayer statlayer = graphics().createImageLayer(bgtile);
    //
    //  statlayer.setWidth(graphics().width());
    // FIXME: problem with graphics.width not being set correctly in html;
    // it always seems to give 640
    //
    statlayer.setHeight(120);//altura do retangulo de informacoes
    layer_.add(statlayer);

    // test: write something in white letters: Pet


    // ------ The text in the status bar as a tripleplay nested layout interface

    // TODO: e o tal do gaps?
    final int mae = 20; // mae == margin on the sides of exlamation
    final int mte = 18; // mae == margin on top of exlamation

    final int mainStatWidth = 200;

    // sm stands for statbar_margin
    TableLayout statbarLayout = new TableLayout(
      COL.minWidth(mainStatWidth).alignLeft().fixed(),
      COL.minWidth(30).stretch()).gaps(mae, mae).alignTop();

//    AxisLayout statbarLayout = new AxisLayout.horizontal().add(
//      COL.minWidth(250).alignLeft().fixed(),
//      COL.minWidth(30).stretch()).gaps(mae, mae).alignTop();
    // the left status plus is the left column
    // the (!) icon plust the right text is the right column

    rightPartLayout_ = new TableLayout(COL.fixed().minWidth(30), COL.alignLeft()).gaps(mae, mae).alignTop();

    exclamacao_ = assets().getImage("pet/images/exclamacao.png");


    // Cria um grupo para os caras da esquerda
    // Basicamente 2 labels: nome grandao e indicadores em fonte menor

    String age1 = idadeCoelhoDiasStr1();
    String age2 = idadeCoelhoDiasStr2();

    mainStat_ = new SizableGroup (AxisLayout.vertical(), mainStatWidth, 0).add (
        new Label("PINGO").addStyles(Styles.make(
            Style.COLOR.is(0xFFFFFFFF),
            Style.HALIGN.left,
            //Style.FONT.is(PlayN.graphics().createFont("Helvetica", Font.Style.PLAIN, 24))
            Style.FONT.is(PlayN.graphics().createFont("EarthboundZero", Font.Style.PLAIN, 31)),
            Style.AUTO_SHRINK.is(true)
        )),
        new Label(age1).addStyles(Styles.make(
            Style.COLOR.is(0xFFFFFFFF),
            Style.HALIGN.left,
            Style.FONT.is(PlayN.graphics().createFont("EarthboundZero", Font.Style.PLAIN, 16)),
            Style.AUTO_SHRINK.is(true)
        )),
        new Label(age2).addStyles(Styles.make(
                    Style.COLOR.is(0xFFFFFFFF),
                    Style.HALIGN.left,
                    Style.FONT.is(PlayN.graphics().createFont("EarthboundZero", Font.Style.PLAIN, 16)),
                    Style.AUTO_SHRINK.is(true)
                ))
        ).addStyles(Styles.make(Style.HALIGN.left));

    rightStatbarGroup_ = new Group(rightPartLayout_).add (
          new Button(Icons.image(exclamacao_)), // FIXME an icon goes here or else blank space w icon's size
          // TODO in future this button will actually be an animation sprite
          new Label("Hello, world!").addStyles(Styles.make(
              Style.COLOR.is(0xFFFFFFFF),
              Style.TEXT_WRAP.is(true),
              Style.HALIGN.left,
              Style.FONT.is(PlayN.graphics().createFont("EarthboundZero", Font.Style.PLAIN, 16))
              ))
          );

    Group statbar = new Group (statbarLayout).add (
        mainStat_,
        rightStatbarGroup_
        ).addStyles(Style.VALIGN.top);

    // create our UI manager and configure it to process pointer events
    statbarIface_ = new Interface();

    //petSheet_.builder().add(Button.class, Style.BACKGROUND.is(Background.blank()));
    Root statbarRoot = statbarIface_.createRoot(new AbsoluteLayout(), petSheet_);

    statbarRoot.setSize(width(), 120); // this includes the secondary buttons

    layer_.addAt(statbarRoot.layer, 0, 0);
    statbarRoot.add(AbsoluteLayout.at(statbar, mae, mte, width()-mae, 120-mte));
  }

  //--------------------------------------------------------------------------------
  private void makeBackgroundInit() {
    bgImageDay_ = assets().getImage("pet/images/cenario_quarto.png");
    bgImageNight_ = assets().getImage("pet/images/cenario_quarto_noite.png");
    bgLayer_ = graphics().createImageLayer(bgImageDay_);
    layer_.addAt(bgLayer_, 0, 120); //janela do quarto do pingo
  }

  //--------------------------------------------------------------------------------
  /* Funcao responsavel por criar os botoes, estes sao colocados em um ArraList */

  private void makeButtons() {
    // create our UI manager and configure it to process pointer events
    iface_ = new Interface();

    //petSheet_.builder().add(Button.class, Style.BACKGROUND.is(Background.blank()));
    Root root_ = iface_.createRoot(new AbsoluteLayout(), petSheet_);

    root_.setSize(width(), 354); // this includes the secondary buttons
    //    root.addStyles(Style.BACKGROUND.is(Background.solid(0xFF99CCFF)));
    layer_.addAt(root_.layer, 0, 442); //Position of buttons

    final Group buttons = new Group(new AbsoluteLayout()).addStyles(
        Style.BACKGROUND.is(Background.blank()));

    // TODO we could use TableLayout in the future but I dont trust it now;
    // I prefer pixel control for now.
    //
    //    Group iface_ = Group(new TableLayout(4).gaps(0, 0)).add(
    //      label("", Background.image(testBg)),
    //    );

    final ArrayList<Image> imgButtSolto =
      new ArrayList<Image>(Arrays.asList(
            assets().getImage("pet/main-buttons/01_comida_principal.png"),
            assets().getImage("pet/main-buttons/02_diversao_principal.png"),
            assets().getImage("pet/main-buttons/03_social_principal.png"),
            assets().getImage("pet/main-buttons/04_higiene_principal.png"),
            assets().getImage("pet/main-buttons/05_obrigacoes_principal.png"),
            assets().getImage("pet/main-buttons/06_saude_principal.png"),
            assets().getImage("pet/main-buttons/07_lazer_principal.png"),
            assets().getImage("pet/main-buttons/08_disciplina_principal.png")
            ));

    final ArrayList<Image> imgButtApertado =
      new ArrayList<Image> (Arrays.asList(
            assets().getImage("pet/main-buttons/01_comida_principal_apertado.png"),
            assets().getImage("pet/main-buttons/02_diversao_principal_apertado.png"),
            assets().getImage("pet/main-buttons/03_social_principal_apertado.png"),
            assets().getImage("pet/main-buttons/04_higiene_principal_apertado.png"),
            assets().getImage("pet/main-buttons/05_obrigacoes_principal_apertado.png"),
            assets().getImage("pet/main-buttons/06_saude_principal_apertado.png"),
            assets().getImage("pet/main-buttons/07_lazer_principal_apertado.png"),
            assets().getImage("pet/main-buttons/08_disciplina_principal_apertado.png")
            ));

    ArrayList< ArrayList<Image> > s_imgButtSecondary = new ArrayList< ArrayList<Image> > (0);

    s_imgButtSecondary.add(
        new ArrayList<Image> (Arrays.asList(
            assets().getImage("pet/main-buttons/011_comida.png"),
            assets().getImage("pet/main-buttons/012_comida.png"),
            assets().getImage("pet/main-buttons/013_comida.png"),
            assets().getImage("pet/main-buttons/014_comida.png")
            )));

    s_imgButtSecondary.add(
        new ArrayList<Image> (Arrays.asList(
            assets().getImage("pet/main-buttons/021_diversao.png"),
            assets().getImage("pet/main-buttons/022_diversao.png"),
            assets().getImage("pet/main-buttons/023_diversao.png"),
            assets().getImage("pet/main-buttons/024_diversao.png")
            )));

    s_imgButtSecondary.add(
        new ArrayList<Image> (0)
        );

    s_imgButtSecondary.add(
        new ArrayList<Image> (Arrays.asList(
            assets().getImage("pet/main-buttons/041_higiene.png"),
            assets().getImage("pet/main-buttons/042_higiene.png"),
            assets().getImage("pet/main-buttons/043_higiene.png"),
            assets().getImage("pet/main-buttons/044_higiene.png")
            )));
    s_imgButtSecondary.add(
        new ArrayList<Image> (Arrays.asList(
            assets().getImage("pet/main-buttons/051_obrigacoes.png"),
            assets().getImage("pet/main-buttons/052_obrigacoes.png")
            )));
    s_imgButtSecondary.add(
        new ArrayList<Image> (Arrays.asList(
            assets().getImage("pet/main-buttons/061_saude.png"),
            assets().getImage("pet/main-buttons/062_saude.png")
            )));
    s_imgButtSecondary.add(
        new ArrayList<Image> (Arrays.asList(
            assets().getImage("pet/main-buttons/071_lazer.png"), // licor
            assets().getImage("pet/main-buttons/072_lazer.png")
            )));
    s_imgButtSecondary.add(
        new ArrayList<Image> (Arrays.asList(
            assets().getImage("pet/main-buttons/081_disciplina.png"),
            assets().getImage("pet/main-buttons/082_disciplina.png"),
            assets().getImage("pet/main-buttons/083_disciplina.png"),
            assets().getImage("pet/main-buttons/084_disciplina.png")
            )));

    final ArrayList< ArrayList<Image> > imgButtSecondary = s_imgButtSecondary;

    /*
      Posicao de cada "butt"
    */
    final int[][] topleft = new int [][] {
      {0, 0},
        {120, 0},
        {240, 0},
        {360, 0},
        {0, 120},
        {120, 120},
        {240, 120},
        {360, 120},
    };

    final int[][] topleftSecondary = new int [][] {
      {0, 0},
        {120, 0},
        {240, 0},
        {360, 0},
    };
    /*-------------------------------------------------------------------------------*/

    final int numMainButts = imgButtSolto.size();
    final ArrayList<Group> sbuttons = new ArrayList<Group>(0);

    for (int b =0; b < numMainButts; ++b) {
      final int bFinal = b;
      ToggleButton but = new ToggleButton (Icons.image(imgButtSolto.get(0)));
      buttons.add(AbsoluteLayout.at(but, topleft[b][0], topleft[b][1], 120, 120));

      // add button b's secondary buttons TODO: use animated sheets for them
      sbuttons.add(new Group(new AbsoluteLayout()).addStyles(
        Style.BACKGROUND.is(Background.solid(0x55FFFFFF))));

      for (int s = 0; s < imgButtSecondary.get(b).size(); ++s) {
        Button sbut = new Button(Icons.image(imgButtSecondary.get(b).get(s)));
        sbuttons.get(b).add(AbsoluteLayout.at(sbut,
          topleftSecondary[s][0], topleftSecondary[s][1], 120, 120));
        but.selected().map(new Function <Boolean, Icon>() {
          public Icon apply (Boolean selected) {
            if (selected)
               return Icons.image(imgButtApertado.get(bFinal));
            else
               return Icons.image(imgButtSolto.get(bFinal));
          }
        }).connectNotify(but.icon.slot());
        // all secondary buttons are added; toggle visibility only
        root_.add(AbsoluteLayout.at(sbuttons.get(bFinal), 0, 0, width(), 120));
        sbuttons.get(bFinal).setVisible(false);
      }

      Selector sel = new Selector(buttons, null);
      root_.add(AbsoluteLayout.at(buttons, 0, 118, width(), 236));

      // TODO: improve this part with a button-> index map so we don't go through
      // all butts
      sel.selected.connect(new Slot<Element<?>>() {
        @Override public void onEmit (Element<?> event) {
          if (event == null) {
            for (Group sb : sbuttons)
              sb.setVisible(false);
          } else {
            for (int i=0; i < numMainButts; ++i) {
              if (buttons.childAt(i) == (ToggleButton) event &&
                  sbuttons.get(i).childCount() != 0) {
                sbuttons.get(i).setVisible(true);
              } else {
                sbuttons.get(i).setVisible(false);
              }
            }
          }
        }
      });
    }
  }

  @Override
  public void init() {
    assert 1 == 2 : "Asserts are on +_+_+_+_+_+_+___+_+__";

    // create a group layer_ to hold everything
    layer_ = graphics().createGroupLayer();
    graphics().rootLayer().add(layer_);
    petSheet_ = PetStyles.newSheet();

    makeStatusbar();
    makeBackgroundInit();
    makeButtons();

    // sprites
    pingo_ = new Pingo(layer_, width() / 2, height() / 2);
    // load attributes

    PetAttributesLoader.CreateAttributes("pet/jsons/atributos.json", beatsCoelhoHora_,
      new Callback<PetAttributes>() {
        @Override
        public void onSuccess(PetAttributes resource) {
          a = resource;
          attributesLoaded = true;
        }

        @Override
        public void onFailure(Throwable err) {
          PlayN.log().error("Error loading pet attributes: " + err.getMessage());
        }
      });
  }

  //--------------------------------------------------------------------------------
  @Override
  public void paint(float alpha) {
    // layers automatically paint themselves (and their children). The rootlayer
    // will paint itself, the background, and the sprites group layer_ automatically
    // so no need to do anything here!

    if (iface_ != null)
      iface_.paint(clock_);

    if (statbarIface_ != null)
      statbarIface_.paint(clock_);
  }

  //--------------------------------------------------------------------------------
  @Override
  public void update(int delta) {
    /*
      Aqui que sao realizadas as atualizacoes dos sprites, sem isto o sprite ficaria estatico
    */
    clock_.update(delta);

    // update clock and passives
    beat_++;

    if (iface_ != null)
      iface_.update(delta);

    if (statbarIface_ != null)
      statbarIface_.update(delta);
  }

}
