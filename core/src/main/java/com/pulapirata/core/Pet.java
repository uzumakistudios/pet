package com.pulapirata.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

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

import com.pulapirata.core.sprites.Pingo;
import com.pulapirata.core.sprites.PingoMorto;
// TODO: we need a generic sprite class; or the layer could automatically update
// them

import react.Function;
import react.UnitSlot;
import react.Slot;

import tripleplay.ui.Element;
import tripleplay.ui.Selector;
import tripleplay.ui.Background;
import tripleplay.ui.Button;
import tripleplay.ui.ImageIcon;
import tripleplay.ui.ToggleButton;
import tripleplay.ui.Label;
import tripleplay.ui.Group;
import tripleplay.ui.Interface;
import tripleplay.ui.Root;
import tripleplay.ui.SimpleStyles;
import tripleplay.ui.Style;
import tripleplay.ui.Styles;
import tripleplay.ui.Stylesheet;
import tripleplay.ui.layout.TableLayout;
import tripleplay.ui.layout.AbsoluteLayout;
import tripleplay.ui.layout.AxisLayout;

import static tripleplay.ui.layout.TableLayout.COL;


public class Pet extends Game.Default {
  protected  static final String STAT_ALERT_1 = "Pingo recebeu convite para ir a um aniversario de um colega na escola.";
  protected  static final String STAT_FILLER_1 = "Idade: %d dias\nAlcool: %d/%d";

  private GroupLayer layer;
  protected Pingo pingo = null;
  protected PingoMorto pingomorto = null;
  protected Group main_stat_;
  // FIXME graphics.width() is weird in html, not respecting #playn-root
  // properties. 
  public int width() { return 480; }
  public int height() { return 800; }


  public static final int UPDATE_RATE = 100; // ms 
  protected final Clock.Source _clock = new Clock.Source(UPDATE_RATE);
  
  
  private int beat = 0; // number of updates

  // the following is not static so that we can dynamically speedup the game if desired
  private int beats_coelhodia = 3; // beats por 1 coelho dia.
  private double beats_coelhosegundo = (double)beats_coelhodia/(24.*60.*60.); 

  public int idade_coelhodias() { return beat / beats_coelhodia; }

  private Interface iface, statbar_iface;

  private int alcool_ = 10;
  private int alcool_passivo_ = -1;
  private int alcool_passivo_beats_ = (int) Math.max(beats_coelhosegundo*60.*60.,1);
  private int alcool_max_ = 10;
  private int alcool_min_ = 0;

  public String idade_coelhodias_str() { return String.format(STAT_FILLER_1, idade_coelhodias(), alcool_, alcool_max_); }
  

  public Pet() {
    super(UPDATE_RATE);
  }


  @Override
  public void init() {
    System.out.println("passivo is " + alcool_passivo_beats_);
    System.out.println("coelho seg " + beats_coelhosegundo);

    // create a group layer to hold everything
    layer = graphics().createGroupLayer();
    graphics().rootLayer().add(layer);
    
    // create and add the status title layer using drawings for faster loading
    CanvasImage bgtile = graphics().createImage(480, 119);
    bgtile.canvas().setFillColor(0xFFFFFFFF);
    bgtile.canvas().fillRect(0, 0, 480, 119);
    bgtile.canvas().setFillColor(0xFF333366);
    bgtile.canvas().fillRect(4, 4, 472, 112);

    ImageLayer statlayer = graphics().createImageLayer(bgtile);
    //
    //  statlayer.setWidth(graphics().width());
    // FIXME: problem with graphics.width not being set correctly in html;
    // it always seems to give 640
    //  
    statlayer.setHeight(120);
    layer.add(statlayer);

    // test: write something in white letters: Pet


    // ------ The text in the status bar as a tripleplay nested layout interface 
  
    // TODO: e o tal do gaps?
    final int mae = 20; // mae == margin on the sides of exlamation
    final int mte = 18; // mae == margin on top of exlamation
    
    // sm stands for statbar_margin
    TableLayout statbar_layout = new TableLayout(COL.minWidth(30).alignLeft(), COL.stretch()).gaps(mae,mae).alignTop();
    // the left status plus is the left column
    // the (!) icon plust the right text is the right column

    TableLayout rightpart_layout = new TableLayout(COL.fixed().minWidth(30), COL.alignLeft()).gaps(mae,mae).alignTop();

    Image exclamacao = assets().getImage("pet/images/exclamacao.png");

    

    // Cria um grupo para os caras da esquerda
    // Basicamente 2 labels: nome grandao e indicadores em fonte menor

    String age = idade_coelhodias_str(); 

    main_stat_ = new Group (AxisLayout.vertical()).add (
        new Label("PINGO").addStyles(Styles.make(
            Style.COLOR.is(0xFFFFFFFF),
            Style.HALIGN.left,
            Style.FONT.is(PlayN.graphics().createFont("Helvetica", Font.Style.PLAIN, 24))
        )),
        new Label(age).addStyles(Styles.make(
            Style.COLOR.is(0xFFFFFFFF),
            Style.HALIGN.left
        ))
    ).addStyles(Styles.make(Style.HALIGN.left));
    // XXX 
    // print out pet's age
    
//    "Idade: " 
//    idade_coelhodias
//    " dias"


    final Group statbar = new Group (statbar_layout).add (
        main_stat_,
        new Group(rightpart_layout).add (
          new Button(new ImageIcon(exclamacao)), // FIXME an icon goes here or else blank space w icon's size
          // TODO in future this button will actually be an animation sprite
          new Label(STAT_ALERT_1).addStyles(Styles.make(
            Style.COLOR.is(0xFFFFFFFF),
            Style.TEXT_WRAP.is(true),
            Style.HALIGN.left
            ))
        )
    ).addStyles(Style.VALIGN.top);

    //    Stylesheet petSheet = SimpleStyles.newSheet();
    Stylesheet petSheet = PetStyles.newSheet();
    
    // create our UI manager and configure it to process pointer events
    statbar_iface = new Interface();
   
    //petSheet.builder().add(Button.class, Style.BACKGROUND.is(Background.blank()));
    Root statbar_root = statbar_iface.createRoot(new AbsoluteLayout(), petSheet);

    statbar_root.setSize(width(), 120); // this includes the secondary buttons

    layer.addAt(statbar_root.layer, 0, 0);
    statbar_root.add(AbsoluteLayout.at(statbar,mae,mte,width()-mae,120-mte));
     
    // ------------------------------------------------------------------

    // create and add background image layer
    Image bgImage = assets().getImage("pet/images/cenario_quarto.png");
    ImageLayer bgLayer = graphics().createImageLayer(bgImage);
    layer.addAt(bgLayer, 0, 120);

    // sprites
    pingo = new Pingo(layer, width() / 2, height() / 2);

    // ------------------------------------------------------------------
    // main buttons
    // TODO
    //   - try the Selector class from tripleplay
    //   - try the click() trigger for the button
    //   - use a sprite implementing the Clicable class then insert it like I
    //   did the button below.
    
    // create our UI manager and configure it to process pointer events
    iface = new Interface();

    //petSheet.builder().add(Button.class, Style.BACKGROUND.is(Background.blank()));
    Root root = iface.createRoot(new AbsoluteLayout(), petSheet);

    root.setSize(width(), 354); // this includes the secondary buttons
    //    root.addStyles(Style.BACKGROUND.is(Background.solid(0xFF99CCFF)));
    layer.addAt(root.layer, 0, 442);

    final Group buttons = new Group(new AbsoluteLayout()).addStyles(
        Style.BACKGROUND.is(Background.blank()));

    // TODO we could use TableLayout in the future but I dont trust it now; 
    // I prefer pixel control for now.
    //
    //    Group iface = Group(new TableLayout(4).gaps(0, 0)).add(
    //      label("", Background.image(testBg)),
    //    );

    final ArrayList<Image> img_butt_solto = 
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

    final ArrayList<Image> img_butt_apertado = 
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

    ArrayList< ArrayList<Image> > s_img_butt_secondary = new ArrayList< ArrayList<Image> > (0);

    s_img_butt_secondary.add(
        new ArrayList<Image> (Arrays.asList(
              assets().getImage("pet/main-buttons/011_comida.png"),
              assets().getImage("pet/main-buttons/012_comida.png"),
              assets().getImage("pet/main-buttons/013_comida.png"),
              assets().getImage("pet/main-buttons/014_comida.png")
    )));

    s_img_butt_secondary.add(
        new ArrayList<Image> (Arrays.asList(
              assets().getImage("pet/main-buttons/021_diversao.png"),
              assets().getImage("pet/main-buttons/022_diversao.png"),
              assets().getImage("pet/main-buttons/023_diversao.png"),
              assets().getImage("pet/main-buttons/024_diversao.png")
    )));
    
    s_img_butt_secondary.add(
        new ArrayList<Image> (0)
    );

    s_img_butt_secondary.add(
        new ArrayList<Image> (Arrays.asList(
              assets().getImage("pet/main-buttons/041_higiene.png"),
              assets().getImage("pet/main-buttons/042_higiene.png"),
              assets().getImage("pet/main-buttons/043_higiene.png"),
              assets().getImage("pet/main-buttons/044_higiene.png")
    )));
    s_img_butt_secondary.add(
        new ArrayList<Image> (Arrays.asList(
              assets().getImage("pet/main-buttons/051_obrigacoes.png"),
              assets().getImage("pet/main-buttons/052_obrigacoes.png")
    )));
    s_img_butt_secondary.add(
        new ArrayList<Image> (Arrays.asList(
              assets().getImage("pet/main-buttons/061_saude.png"),
              assets().getImage("pet/main-buttons/062_saude.png")
    )));
    s_img_butt_secondary.add(
        new ArrayList<Image> (Arrays.asList(
              assets().getImage("pet/main-buttons/071_lazer.png"), // licor
              assets().getImage("pet/main-buttons/072_lazer.png")
    )));
    s_img_butt_secondary.add(
        new ArrayList<Image> (Arrays.asList(
              assets().getImage("pet/main-buttons/081_disciplina.png"),
              assets().getImage("pet/main-buttons/082_disciplina.png"),
              assets().getImage("pet/main-buttons/083_disciplina.png"),
              assets().getImage("pet/main-buttons/084_disciplina.png")
    )));

    final ArrayList< ArrayList<Image> > img_butt_secondary = s_img_butt_secondary;

    final int[][] topleft = new int [][] {
      {0,0},
      {120,0},
      {240,0},
      {360,0},
      {0,120},
      {120,120},
      {240,120},
      {360,120},
    };

    final int[][] topleft_secondary = new int [][] {
      {0,0},
      {120,0},
      {240,0},
      {360,0},
    };

    final int num_main_butts = img_butt_solto.size();
    final ArrayList<Group> sbuttons = new ArrayList<Group>(0);

    for (int b =0; b < num_main_butts; ++b) {
      final int b_final = b;
      ToggleButton but = new ToggleButton (new ImageIcon(img_butt_solto.get(0)));
      buttons.add(AbsoluteLayout.at(but, topleft[b][0], topleft[b][1], 120, 120));

      // add button b's secondary buttons TODO: use animated sheets for them
      
      sbuttons.add(new Group(new AbsoluteLayout()).addStyles(
        Style.BACKGROUND.is(Background.solid(0x55FFFFFF))));

      for (int s = 0; s < img_butt_secondary.get(b).size(); ++s) {
        Button sbut = new Button(img_butt_secondary.get(b).get(s));
        sbuttons.get(b).add(AbsoluteLayout.at(sbut, 
          topleft_secondary[s][0], topleft_secondary[s][1], 120, 120));

        if (b == 6 // diversao
        &&  s == 0) // licor
          sbut.clicked().connect(new UnitSlot() {
            public void onEmit() {
              alcool_ = alcool_max_; // TODO modificar de acordo com folha
            }
          });

      }

      but.selected.map(new Function <Boolean, ImageIcon>() {
        public ImageIcon apply (Boolean selected) {
               if (selected) {
                  return new ImageIcon(img_butt_apertado.get(b_final));
               } else {
                  return new ImageIcon(img_butt_solto.get(b_final));
               }
      }}).connectNotify(but.icon.slot());

      // all secondary buttons are added; toggle visibility only
      root.add(AbsoluteLayout.at(sbuttons.get(b_final), 0, 0, width(), 120));
      sbuttons.get(b_final).setVisible(false);
    }

    Selector sel = new Selector(buttons, null);
    root.add(AbsoluteLayout.at(buttons, 0, 118, width(), 236));

    // TODO: improve this part with a button-> index map so we don't go through
    // all butts
    sel.selected.connect(new Slot<Element<?>>() {
      @Override public void onEmit (Element<?> event) {
        if (event == null) {
          for (Group sb : sbuttons) {
            sb.setVisible(false);
          }
        } else {
          for (int i=0; i < num_main_butts; ++i) {
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


    /* Exemplo p/ sinais e eventos
     *
    but0.clicked().connect(new UnitSlot() {
        @Override
        public void onEmit() {
          System.out.println("but0 clicked!");
          but0.icon.update(but0press);
        }
    });
    */
  }


  @Override
  public void paint(float alpha) {
    // layers automatically paint themselves (and their children). The rootlayer
    // will paint itself, the background, and the sprites group layer automatically
    // so no need to do anything here!

    if (iface != null) {
      iface.paint(_clock);
    }
    if (statbar_iface != null) {
      statbar_iface.paint(_clock);
    }
  }

  @Override
  public void update(int delta) {
    _clock.update(delta);
    if (pingo != null)
      pingo.update(delta);

    if(beat / beats_coelhodia >= 30) {
      if (pingomorto == null) {
          // pingo morre
          // beat = beat; // pass
          //pingos.del(pingo);
        pingomorto = new PingoMorto(layer, width() / 2, height() / 2);
        pingo.detatch(layer);
        pingo = null;
      } else {
        pingomorto.update(delta);
      }
    } else {
      beat = beat + 1;

      if ((beat % alcool_passivo_beats_) == 0)
        if (alcool_ > alcool_min_)
          alcool_ += alcool_passivo_;

      Label l = (Label) main_stat_.childAt(1);
      l.text.update(idade_coelhodias_str());
    }

    if (iface != null) {
      iface.update(delta);
    }
    if (statbar_iface != null) {
      statbar_iface.update(delta);
    }
  }

}
