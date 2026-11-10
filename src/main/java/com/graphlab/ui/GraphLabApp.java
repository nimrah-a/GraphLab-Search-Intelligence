package com.graphlab.ui;

import com.graphlab.algorithms.*;
import com.graphlab.forensics.*;
import com.graphlab.model.*;
import com.graphlab.search.SearchNetwork;
import static com.graphlab.forensics.SyntheticCases.*;
import javafx.application.Application;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import java.util.*;

/** Interactive fictional case and forensic comparison studio. */
public final class GraphLabApp extends Application {
    private final List<CaseRecord> cases=SyntheticCases.all();
    private CaseRecord current=cases.get(0);
    private final Graph map=SearchNetwork.build();
    private final Set<Edge> blocked=new LinkedHashSet<>();
    private Set<Edge> highlighted=new HashSet<>();
    private final BorderPane main=new BorderPane();
    private final Label status=new Label("Synthetic educational dataset · no real personal information");
    private final Label headline=new Label();
    private final Label subtitle=new Label();
    private final ComboBox<CaseRecord> picker=new ComboBox<>();
    private final Canvas networkCanvas=new Canvas(570,385);
    private final Label mapResult=new Label();
    private final ComboBox<String> algorithm=new ComboBox<>();
    private final ComboBox<String> target=new ComboBox<>();
    private final ComboBox<String> closure=new ComboBox<>();
    private final VBox forensicContent=new VBox(16);
    private final Label evidenceNote=new Label();
    private String page="overview";
    private static final String BG="#111D23", PANEL="#1B2C31", TEXT="#F5F0E8", MUTED="#A8B6B3", CYAN="#67C6FF";
    private final Set<String> reviewedEvidence = new LinkedHashSet<>();
    private boolean showLandmarks=true;
    private int simulationStep=0;
    private Timeline playback;
    private final Map<String,Integer> simulatedProgress=new HashMap<>();
    private record PersonProfile(String name,int age,String sex,String height,String eyes,String hair,
         String lastSeen,String missingFor,String location,String clothing,String possessions,String identifying,
         String reportSource,String urgency,String note) {}
    private PersonProfile profile(){
        return switch(current.id()){
            case "MP-002" -> new PersonProfile("Jordan Ellis",34,"Male","5 ft 11 in","Hazel","Black, short",
               "22 Oct 2026 · 20:15","18 days","North Park parking entrance",
               "Navy jacket, black trousers, brown boots","Canvas backpack, key ring",
               "Small scar above left eyebrow","Family report / simulated","Elevated",
               "A parking entrance sighting is uncorroborated. The timeline includes a second observation that has not been verified.");
            case "MP-003" -> new PersonProfile("Amara Collins",27,"Female","5 ft 6 in","Brown","Black, curly",
               "09 Sep 2026 · 17:40","2 months","Harbor Walk promenade",
               "Charcoal hoodie, gray joggers, white sneakers","Black shoulder bag, phone",
               "Silver bracelet on left wrist","Friend report / simulated","Open / prolonged",
               "The chronology has an unresolved gap. Available comparison material is partial, so no identity conclusion is warranted.");
            case "MP-004" -> new PersonProfile("Noah Bennett",16,"Male","5 ft 8 in","Blue","Light brown",
               "08 Nov 2026 · 19:10","1 day","East Terminal pedestrian path",
               "Green windbreaker, black athletic trousers","Blue bicycle helmet, school bag",
               "Small birthmark near right jaw","Guardian report / simulated","High priority",
               "The fictional subject is a minor. Early search coordination is emphasized; the sample impression is unusable.");
            default -> new PersonProfile("Elena Rivera",21,"Female","5 ft 4 in","Brown","Dark brown, shoulder-length",
               "06 Nov 2026 · 18:40","3 days","Riverside university transit station",
               "Tan coat, blue jeans, white trainers","Canvas tote, phone",
               "Small crescent-shaped pendant","Roommate report / simulated","Priority review",
               "Last confirmed observation is near the campus-to-transit corridor. Later sightings remain unverified.");
        };
    }
    private void stopPlayback(){ if(playback!=null){playback.stop(); playback=null;} }
    private void advanceSimulation(){
        int next=Math.min(4,simulatedProgress.getOrDefault(current.id(),0)+1);
        simulatedProgress.put(current.id(),next);
        if("overview".equals(page)) refresh();
        if(next>=4) stopPlayback();
    }
    private void playSimulation(){
        stopPlayback();
        playback=new Timeline(new KeyFrame(Duration.seconds(1.8),e->advanceSimulation()));
        playback.setCycleCount(Timeline.INDEFINITE);playback.play();
    }

    @Override public void start(Stage stage){
        picker.getItems().addAll(cases);
        picker.setCellFactory(v->new ListCell<>() {protected void updateItem(CaseRecord c,boolean empty){super.updateItem(c,empty);setText(empty||c==null?null:c.id()+" · "+c.title());}});
        picker.setButtonCell(new ListCell<>() {protected void updateItem(CaseRecord c,boolean empty){super.updateItem(c,empty);setText(empty||c==null?null:c.id()+" · "+c.title());}});
        picker.setValue(current);
        picker.setOnAction(e->{stopPlayback();current=picker.getValue();refresh();});
        var root=new BorderPane();root.setStyle("-fx-background-color:"+BG+";");
        root.setLeft(sidebar());root.setCenter(main);root.setBottom(bottom());
        Scene scene=new Scene(root,1330,820);scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/search.css")).toExternalForm());
        stage.setTitle("GraphLab • Search Intelligence | Fictional Simulation");stage.setMinWidth(1140);stage.setMinHeight(720);stage.setScene(scene);stage.setOnCloseRequest(e->stopPlayback());stage.show();
        navigate("overview");
    }
    private VBox sidebar(){
        VBox nav=new VBox(12);nav.getStyleClass().add("sidebar");nav.setPrefWidth(224);
        Label brand=new Label("◈  GRAPHLAB");brand.getStyleClass().add("brand");
        Label tag=new Label("INVESTIGATIVE WORKSPACE");tag.getStyleClass().add("sidebar-subtitle");
        nav.getChildren().addAll(brand,tag,new Separator(),section("WORKSPACE"),
          navButton("▦  Command center","overview"),navButton("◎  Search operations","map"),
          navButton("⌕  Forensic records","forensics"),navButton("≡  Evidence ledger","timeline"));
        Region spacer=new Region();VBox.setVgrow(spacer,Priority.ALWAYS);
        Label disclaimer=new Label("SYNTHETIC CASES ONLY\nEducational simulation\nNo real biometric records");disclaimer.getStyleClass().add("muted");disclaimer.setWrapText(true);
        nav.getChildren().addAll(spacer,new Separator(),disclaimer);
        return nav;
    }
    private Button navButton(String text,String id){Button b=new Button(text);b.getStyleClass().add("nav-button");b.setMaxWidth(Double.MAX_VALUE);b.setOnAction(e->navigate(id));return b;}
    private HBox bottom(){status.getStyleClass().add("status");HBox box=new HBox(status);box.getStyleClass().add("footer");return box;}
    private void navigate(String id){if(!"overview".equals(id)) stopPlayback();page=id;refresh();}
    private void refresh(){
        main.getChildren().clear();VBox content=new VBox(17);content.getStyleClass().add("content");
        ScrollPane scroll=new ScrollPane(content);scroll.setFitToWidth(true);scroll.getStyleClass().add("main-scroll");
        main.setCenter(scroll);
        HBox header=new HBox(20);header.setAlignment(Pos.CENTER_LEFT);
        VBox labels=new VBox(5);headline.setText(switch(page){case "map"->"Search operations";case "forensics"->"Forensic records";case "timeline"->"Evidence ledger";default->"Investigation command center";});headline.getStyleClass().add("heading");
        subtitle.setText("Fictional exercise  /  "+current.id()+"  /  "+current.title());subtitle.getStyleClass().add("subheading");labels.getChildren().addAll(headline,subtitle);Region spacer=new Region();HBox.setHgrow(spacer,Priority.ALWAYS);picker.setPrefWidth(275);header.getChildren().addAll(labels,spacer,picker);content.getChildren().add(header);
        switch(page){case "map"->renderMap(content);case "forensics"->renderForensics(content);case "timeline"->renderTimeline(content);default->renderOverview(content);}
        status.setText("Fictional case "+current.id()+"   •   No actual forensic identification performed");
    }
    private record CaseDetails(String opened,String region,String lead,String summary,String[] events) {}
    private CaseDetails details(){
        return switch(current.id()){
            case "MP-002" -> new CaseDetails("09 NOV 2026 · 08:20","NORTH PARK SECTOR","West entrance / public park","A fictional observation near the west entrance initiated a local area search. A second record requires source verification.",new String[]{"08:20|Case opened|Dispatch desk|INTAKE","08:44|West entrance observation logged|Witness record|UNVERIFIED","09:12|Park footpath checked|Search team|REVIEWED","09:46|Synthetic comparison item uploaded|Forensic lab|PENDING"});
            case "MP-003" -> new CaseDetails("09 NOV 2026 · 17:12","HARBOR WALK SECTOR","Promenade / transit access","The training scenario includes an incomplete timeline and partial comparison material. Maintain uncertainty during review.",new String[]{"17:12|Case opened|Intake desk|INTAKE","17:38|Promenade camera record indexed|Media log|UNVERIFIED","18:05|Transit platform search assigned|Search team|PENDING","18:33|Partial synthetic print received|Forensic lab|REVIEWED"});
            case "MP-004" -> new CaseDetails("09 NOV 2026 · 21:05","EAST TERMINAL SECTOR","Terminal concourse / nearby road","Initial synthetic evidence is unsuitable for comparison; route coordination is modeled separately from identification.",new String[]{"21:05|Case opened|Dispatch desk|INTAKE","21:18|Terminal exit noted|Observation log|UNVERIFIED","21:44|Search grid prepared|Search coordinator|PENDING","22:03|Unusable impression archived|Forensic lab|REVIEWED"});
            default -> new CaseDetails("09 NOV 2026 · 18:40","RIVERSIDE SECTOR","Campus / rail interchange","A fictional last-known-location report near a university transit corridor. Explore search logistics and review synthetic evidence without presuming an outcome.",new String[]{"18:40|Last-known-location report|Intake desk|UNVERIFIED","19:05|Search sectors initialized|Search coordinator|REVIEWED","19:20|Synthetic fingerprint sample logged|Forensic lab|PENDING","19:35|Synthetic dental reference attached|Records desk|PENDING"});
        };
    }
    private Label textLabel(String content,String style){Label label=new Label(content);label.setWrapText(true);label.getStyleClass().add(style);return label;}
    private HBox buttons(Button... buttons){HBox row=new HBox(10,buttons);row.setAlignment(Pos.CENTER_LEFT);return row;}
    private VBox infoCell(String label,String value){VBox cell=new VBox(5,section(label),textLabel(value,"field-value"));cell.getStyleClass().add("info-cell");return cell;}
    private Canvas portrait(PersonProfile p){
        Canvas c=new Canvas(156,190); GraphicsContext g=c.getGraphicsContext2D();
        g.setFill(Color.web("#15283A"));g.fillRoundRect(0,0,156,190,14,14);
        g.setStroke(Color.web("#294862"));g.setLineWidth(1);
        for(int i=0;i<156;i+=18) g.strokeLine(i,0,i,190);
        g.setFill(Color.web("#385A78"));g.fillOval(46,32,64,64);
        g.fillRoundRect(24,104,108,75,36,36);
        g.setStroke(Color.web("#71C9FF"));g.setLineWidth(1.5);g.strokeRoundRect(2,2,152,186,12,12);
        g.setFill(Color.web("#D9EAF9"));g.setFont(Font.font("System",FontWeight.BOLD,10));g.fillText("ILLUSTRATIVE PROFILE",17,17);
        g.setFill(Color.web("#A9C4DC"));g.setFont(Font.font(10));g.fillText("NO ACTUAL PHOTOGRAPH",17,181);
        return c;
    }
    private HBox profileCard(PersonProfile p){
        HBox card=new HBox(20); card.getStyleClass().add("card"); card.setAlignment(Pos.TOP_LEFT);
        VBox identity=new VBox(12,portrait(p),textLabel("SIMULATED PROFILE · "+current.id(),"eyebrow"));identity.setMinWidth(160);
        VBox details=new VBox(12);HBox.setHgrow(details,Priority.ALWAYS);
        details.getChildren().addAll(textLabel(p.name(),"profile-name"),textLabel("LAST CONFIRMED SIGHTING  ·  "+p.lastSeen(),"eyebrow"));
        GridPane grid=new GridPane();grid.setHgap(11);grid.setVgap(11);
        VBox[] cells={infoCell("AGE",p.age()+" years"),infoCell("SEX",p.sex()),infoCell("HEIGHT",p.height()),infoCell("EYES",p.eyes()),infoCell("HAIR",p.hair()),infoCell("TIME MISSING",p.missingFor())};
        for(int i=0;i<cells.length;i++){grid.add(cells[i],i%3,i/3);cells[i].setMaxWidth(Double.MAX_VALUE);GridPane.setHgrow(cells[i],Priority.ALWAYS);}
        details.getChildren().addAll(grid,section("LAST SEEN WEARING"),textLabel(p.clothing(),"body-copy"),
           section("PERSONAL EFFECTS"),textLabel(p.possessions(),"body-copy"),
           section("DISTINGUISHING FEATURE"),textLabel(p.identifying(),"body-copy"));
        card.getChildren().addAll(identity,details);return card;
    }
    private VBox activityPanel(){
        PersonProfile p=profile();int step=simulatedProgress.getOrDefault(current.id(),0);
        VBox panel=panel("Investigation simulation", "Play a fictional sequence of investigative events or advance one event at a time. Nothing here represents live case data.");
        ProgressBar progress=new ProgressBar(step/4.0);progress.setMaxWidth(Double.MAX_VALUE);progress.setPrefHeight(9);
        Label label=textLabel("SIMULATION PROGRESS  ·  "+step+" / 4 EVENTS","eyebrow");
        Button play=action("▶ Play");play.setDisable(step>=4);play.setOnAction(e->playSimulation());
        Button pause=secondary("Ⅱ Pause");pause.setOnAction(e->stopPlayback());
        Button next=secondary("Next event →");next.setDisable(step>=4);next.setOnAction(e->{stopPlayback();advanceSimulation();});
        Button reset=secondary("Reset");reset.setOnAction(e->{stopPlayback();simulatedProgress.put(current.id(),0);refresh();});
        panel.getChildren().addAll(label,progress,buttons(play,pause,next,reset));
        VBox events=new VBox(7);String[] names={"Case intake initialized","Search sectors assigned","Evidence record indexed","Forensic comparison available"};
        for(int i=0;i<4;i++){
            Label row=textLabel((i<step?"●  ":"○  ")+names[i]+(i<step?"  ·  LOGGED":"  ·  QUEUED"),i<step?"sim-event-done":"sim-event");events.getChildren().add(row);
        }
        panel.getChildren().addAll(section("SIMULATED ACTIVITY FEED"),events);
        return panel;
    }
    private void renderOverview(VBox out){
        CaseDetails d=details();PersonProfile p=profile();
        VBox hero=new VBox(12);hero.getStyleClass().add("hero");
        HBox top=new HBox(12);top.setAlignment(Pos.CENTER_LEFT);
        Label tag=textLabel("ACTIVE CASE DOSSIER  /  "+current.id(),"hero-tag");Region grow=new Region();HBox.setHgrow(grow,Priority.ALWAYS);
        Label chip=textLabel("●  "+p.urgency().toUpperCase()+" · FICTIONAL","hero-chip");top.getChildren().addAll(tag,grow,chip);
        Label title=textLabel(p.name(),"hero-title");
        Label summary=textLabel(p.missingFor()+" missing   ·   Last seen at "+p.location(),"hero-desc");
        Label note=textLabel(p.note(),"muted");note.setMaxWidth(950);
        Button openMap=action("Open search operations →");openMap.setOnAction(e->navigate("map"));
        Button openEvidence=secondary("Review evidence");openEvidence.setOnAction(e->navigate("timeline"));
        Button forensics=secondary("Forensic workspace");forensics.setOnAction(e->navigate("forensics"));
        hero.getChildren().addAll(top,title,summary,note,buttons(openMap,openEvidence,forensics));out.getChildren().add(hero);
        HBox kpis=new HBox(12);
        for(VBox cell:List.of(infoCell("TIME MISSING",p.missingFor()),infoCell("LAST SEEN",p.lastSeen()),
             infoCell("SEARCH SECTOR",d.region()),infoCell("EVIDENCE REVIEW",reviewedCount()+" / 4 reviewed"))){
           HBox.setHgrow(cell,Priority.ALWAYS);cell.setMaxWidth(Double.MAX_VALUE);kpis.getChildren().add(cell);
        }out.getChildren().add(kpis);
        out.getChildren().add(profileCard(p));
        HBox columns=new HBox(16);columns.setAlignment(Pos.TOP_LEFT);
        VBox simulation=activityPanel();simulation.setPrefWidth(600);HBox.setHgrow(simulation,Priority.ALWAYS);
        VBox evidence=panel("Case briefing & evidence", "Reporting source: "+p.reportSource()+" · "+d.region());evidence.setPrefWidth(360);
        evidence.getChildren().addAll(section("INITIAL BRIEFING"),textLabel(d.summary(),"body-copy"));
        for(String event:d.events()){
            String[] parts=event.split("\\|");
            HBox entry=new HBox(9);entry.setAlignment(Pos.CENTER_LEFT);entry.getStyleClass().add("compact-row");
            VBox lines=new VBox(2,textLabel(parts[1],"row-heading"),textLabel(parts[0]+" · "+parts[2],"muted"));HBox.setHgrow(lines,Priority.ALWAYS);
            entry.getChildren().addAll(lines,textLabel(reviewedEvidence.contains(current.id()+"-"+parts[0])?"REVIEWED":parts[3],"pill"));evidence.getChildren().add(entry);
        }
        Button ledger=secondary("Open evidence register →");ledger.setOnAction(e->navigate("timeline"));evidence.getChildren().add(ledger);
        columns.getChildren().addAll(simulation,evidence);out.getChildren().add(columns);
        VBox methodology=panel("Model scope", "The case, people, locations and biometric data are synthetic. Search algorithms model hypothetical routes and network construction; outputs are never predictions of actual whereabouts or forensic identifications.");
        out.getChildren().add(methodology);
    }
    private long reviewedCount(){return reviewedEvidence.stream().filter(e->e.startsWith(current.id()+"-")).count();}
    private Button secondary(String t){Button b=new Button(t);b.getStyleClass().add("secondary-button");return b;}
    private HBox metric(String label,String value){VBox tile=new VBox(7);tile.getStyleClass().add("card");Label top=section(label);Label bottom=new Label(value);bottom.getStyleClass().add("metric-value");bottom.setWrapText(true);tile.getChildren().addAll(top,bottom);HBox.setHgrow(tile,Priority.ALWAYS);tile.setMaxWidth(Double.MAX_VALUE);return new HBox(tile);}
    private void renderMap(VBox out){
        out.getChildren().add(textLabel("CASE SUBJECT  ·  "+profile().name()+"  /  LAST SEEN: "+profile().location()+"  /  "+profile().missingFor()+" MISSING","eyebrow"));
        Label lead=new Label("Test travel routes and minimum-cost search-station connectivity. Weights are hypothetical minutes or infrastructure cost units depending on algorithm.");lead.getStyleClass().add("lead");lead.setWrapText(true);out.getChildren().add(lead);
        HBox controls=new HBox(12);controls.setAlignment(Pos.CENTER_LEFT);
        algorithm.getItems().setAll("Dijkstra · shortest route","BFS · exploration order","Kruskal · minimum network","Prim · minimum network");if(algorithm.getValue()==null)algorithm.setValue(algorithm.getItems().get(0));
        target.getItems().setAll("Transit","Park","Café","Clinic","Search HQ");if(target.getValue()==null)target.setValue("Clinic");
        closure.getItems().clear();closure.getItems().add("No closure");for(Edge e:map.edges())closure.getItems().add(edgeText(e));if(closure.getValue()==null||!closure.getItems().contains(closure.getValue()))closure.setValue("No closure");
        Button run=action("Run algorithm");run.setOnAction(e->runMap());controls.getChildren().addAll(algorithm,target,closure,run);out.getChildren().add(controls);
        HBox workspace=new HBox(16);workspace.setAlignment(Pos.TOP_LEFT);
        VBox mapBox=new VBox(10);mapBox.getStyleClass().add("card");mapBox.getChildren().addAll(section("SEARCH AREA  /  SCHEMATIC NETWORK"),networkCanvas);
        mapBox.setMaxWidth(610);
        VBox right=panel("Algorithm insights","Select a method and click Run algorithm. The network will highlight the computed result.");
        mapResult.setText("Ready for graph analysis.");mapResult.setWrapText(true);mapResult.getStyleClass().add("result-text");right.getChildren().add(mapResult);right.setPrefWidth(290);
        workspace.getChildren().addAll(mapBox,right);out.getChildren().add(workspace);
        Label note=new Label("Routes and time values are fictional. Minimum spanning trees optimize total connection weight, not travel time between two locations. This simulation does not predict a missing person's whereabouts.");note.setWrapText(true);note.getStyleClass().add("muted");out.getChildren().add(note);drawMap();
    }
    private String edgeText(Edge e){return e.from().id()+" ↔ "+e.to().id()+" · "+e.weight();}
    private void runMap(){
        blocked.clear();String choice=closure.getValue();if(choice!=null&&!choice.equals("No closure"))for(Edge e:map.edges())if(edgeText(e).equals(choice)){blocked.add(e);break;}
        highlighted=new LinkedHashSet<>();String mode=algorithm.getValue();
        if(mode.startsWith("Dijkstra")){
            String destination=switch(target.getValue()){case "Transit"->"STN";case "Park"->"PARK";case "Café"->"CAFE";case "Clinic"->"CLINIC";default->"HQ";};
            var p=SearchNetwork.dijkstra(map,"CAMP",destination,blocked);
            for(int i=1;i<p.nodes().size();i++){Vertex x=p.nodes().get(i-1),y=p.nodes().get(i);for(Edge e:map.edges())if((e.from().equals(x)&&e.to().equals(y))||(e.from().equals(y)&&e.to().equals(x)))highlighted.add(e);}
            mapResult.setText(p.nodes().isEmpty()?"No reachable route under the selected closure.":"Shortest path: "+String.join(" → ",p.nodes().stream().map(Vertex::id).toList())+"\nTravel time: "+p.total()+" hypothetical minutes");
        }else if(mode.startsWith("BFS")){
            var visited=SearchNetwork.bfs(map,"CAMP",blocked);
            mapResult.setText("Reachable site exploration order:\n"+String.join(" → ",visited.stream().map(Vertex::id).toList())+"\n\nBFS is traversal, not a shortest-time calculation.");
        }else{
            Graph usable=new Graph();for(Vertex v:map.vertices())usable.addVertex(v.id());for(Edge e:map.edges())if(!blocked.contains(e))usable.addEdge(e.from().id(),e.to().id(),e.weight());
            MSTResult result=mode.startsWith("Prim")?PrimMST.compute(usable):KruskalMST.compute(usable);
            for(Edge e:result.selectedEdges())for(Edge candidate:map.edges())if(candidate.equals(e))highlighted.add(candidate);
            mapResult.setText(result.algorithm()+"\nTotal network cost: "+result.totalWeight()+" units\nSelected links: "+result.selectedEdges().size()+"\nConnected groups: "+result.componentCount()+"\n\nInfrastructure connectivity, not passenger routing.");
        }
        drawMap();
    }
    private void drawMap(){
        GraphicsContext g=networkCanvas.getGraphicsContext2D();double w=networkCanvas.getWidth(),h=networkCanvas.getHeight();g.setFill(Color.web("#101C2B"));g.fillRoundRect(0,0,w,h,14,14);
        g.setStroke(Color.web("#243B55"));g.setLineWidth(.5);for(int x=10;x<w;x+=22)g.strokeLine(x,0,x,h);for(int y=10;y<h;y+=22)g.strokeLine(0,y,w,y);
        for(Edge e:map.edges()){
            var a=site(e.from().id());var b=site(e.to().id());boolean isBlocked=blocked.contains(e),isActive=highlighted.contains(e);
            g.setStroke(Color.web(isBlocked?"#E88981":isActive?"#67C6FF":"#526C8D"));g.setLineWidth(isActive?4:2);g.setLineDashes(isBlocked?7:0);g.strokeLine(a.x(),a.y(),b.x(),b.y());g.setLineDashes();
            double tx=(a.x()+b.x())/2,ty=(a.y()+b.y())/2;g.setFill(Color.web("#1B3550"));g.fillRoundRect(tx-12,ty-12,24,19,6,6);g.setFill(Color.WHITE);g.setFont(Font.font(11));g.fillText(String.valueOf((int)e.weight()),tx-4,ty+2);
        }
        for(var s:SearchNetwork.SITES){g.setFill(Color.web("#254464"));g.fillOval(s.x()-20,s.y()-20,40,40);g.setStroke(Color.web(CYAN));g.setLineWidth(2);g.strokeOval(s.x()-20,s.y()-20,40,40);g.setFill(Color.WHITE);g.setFont(Font.font("System",FontWeight.BOLD,10));g.fillText(s.id(),s.x()-Math.min(17,s.id().length()*3),s.y()+4);g.setFill(Color.web("#D7E9F8"));g.fillText(s.name(),s.x()-20,s.y()+35);}
    }
    private SearchNetwork.Site site(String id){return SearchNetwork.SITES.stream().filter(s->s.id().equals(id)).findFirst().orElseThrow();}
    private void renderForensics(VBox out){
        out.getChildren().add(textLabel("RECORD SUBJECT  ·  "+profile().name()+"   |   "+current.id()+"   |   FICTITIOUS BIOMETRIC RECORDS","eyebrow"));
        Label lead=new Label("Examine synthetic fingerprint landmarks and tooth-level findings. Results are automatically computed from demo comparison rules—never from invented identity probabilities.");lead.setWrapText(true);lead.getStyleClass().add("lead");out.getChildren().add(lead);
        TabPane tabs=new TabPane();tabs.getStyleClass().add("lab-tabs");Tab fingerprints=new Tab("Fingerprint comparison");fingerprints.setClosable(false);Tab dental=new Tab("Dental records");dental.setClosable(false);
        fingerprints.setContent(fingerprintPanel());dental.setContent(dentalPanel());tabs.getTabs().addAll(fingerprints,dental);out.getChildren().add(tabs);
    }
    private Node fingerprintPanel(){
        VBox view=new VBox(15);view.setPadding(new Insets(18));
        HBox metadata=new HBox(10);metadata.getChildren().addAll(infoCell("REFERENCE ID",current.id()+"-FP-R01"),infoCell("QUESTIONED ID",current.id()+"-FP-Q01"),infoCell("RECORD TYPE","Synthetic ridge illustrations"));
        view.getChildren().add(metadata);
        CheckBox landmarks=new CheckBox("Display synthetic landmark overlays");landmarks.setSelected(showLandmarks);
        HBox prints=new HBox(16);Canvas a=new Canvas(345,255),b=new Canvas(345,255);Runnable redraw=()->{drawPrint(a,current.reference(),"REFERENCE / FP-R01");drawPrint(b,current.recovered(),"QUESTIONED / FP-Q01");};
        landmarks.setOnAction(e->{showLandmarks=landmarks.isSelected();redraw.run();});redraw.run();prints.getChildren().addAll(a,b);
        view.getChildren().addAll(landmarks,prints);
        Label outcome=textLabel("Select Analyze comparison to see the synthetic findings and the features used to explain them.","result-text");
        Button compare=action("Analyze comparison  →");compare.setOnAction(e->{var result=ForensicComparison.fingerprint(current.reference(),current.recovered());outcome.setText("SIMULATED FINDING  /  "+result.finding().name().replace('_',' ') +"\n\n"+result.explanation()+"\n\nOBSERVATIONS\n• "+String.join("\n• ",result.observations()));});
        Button reset=secondary("Reset finding");reset.setOnAction(e->outcome.setText("Comparison not yet analyzed. Synthetic source data remains unchanged."));
        view.getChildren().addAll(buttons(compare,reset),outcome,disclaimer());return view;
    }
    private void drawPrint(Canvas canvas,Fingerprint fingerprint,String title){
        GraphicsContext g=canvas.getGraphicsContext2D();g.setFill(Color.web("#101C2B"));g.fillRoundRect(0,0,canvas.getWidth(),canvas.getHeight(),14,14);
        g.setFill(Color.web("#D7E7DE"));g.setFont(Font.font("System",FontWeight.BOLD,12));g.fillText(title,16,25);
        g.setStroke(Color.web("#83B6D9"));g.setLineWidth(1.7);
        for(int i=0;i<15;i++){double xx=85+i*3.4, yy=135+(i%3)*1.2;g.strokeOval(xx-45-i*3.4,yy-67-i*2.7,100+i*6.5,120+i*5.5);}
        if(showLandmarks) for(var p:fingerprint.features()){
            double x=72+p.x()*2.1,y=45+p.y()*1.75;g.setFill(Color.web("#A8DBFF"));g.fillOval(x-3,y-3,6,6);g.setStroke(Color.web("#A8DBFF"));g.strokeOval(x-7,y-7,14,14);
        }
        g.setFill(Color.web("#A9BED2"));g.setFont(Font.font(11));g.fillText(fingerprint.features().size()+" landmarks  ·  simulated quality tier: "+(fingerprint.quality()>=70?"HIGH":fingerprint.quality()>=30?"PARTIAL":"POOR"),15,239);
    }
    private Node dentalPanel(){
        VBox view=new VBox(15);view.setPadding(new Insets(18));
        Label intro=new Label("Select a tooth in either record to inspect the stored synthetic observation. FDI tooth numbering is used.");intro.getStyleClass().add("muted");intro.setWrapText(true);view.getChildren().add(intro);
        HBox charts=new HBox(16);Label detail=new Label("Click a tooth tile to inspect its observation.");detail.getStyleClass().add("result-text");detail.setWrapText(true);
        charts.getChildren().addAll(dentalChart(current.dentalReference(),detail),dentalChart(current.dentalRecovered(),detail));view.getChildren().addAll(charts,detail);
        VBox summary=panel("Tooth-by-tooth record audit","Comparison of the listed fictional tooth observations. Click individual chart tiles above for a focused inspection.");
        GridPane audit=new GridPane();audit.setHgap(12);audit.setVgap(7);
        String[] headers={"FDI","REFERENCE","QUESTIONED","ASSESSMENT"};
        for(int j=0;j<headers.length;j++)audit.add(section(headers[j]),j,0);
        int row=1;for(int t:new int[]{11,12,13,14,15,16,17,18,21,22,23,24,25,26,27,28}){
            String ref=current.dentalReference().chart().get(t),rec=current.dentalRecovered().chart().get(t);
            String verdict=rec==null?"NOT RECORDED":Objects.equals(ref,rec)?"CONSISTENT":"DISCREPANCY";
            audit.add(textLabel(String.valueOf(t),"audit-cell"),0,row);audit.add(textLabel(ref==null?"—":ref,"audit-cell"),1,row);
            audit.add(textLabel(rec==null?"—":rec,"audit-cell"),2,row);audit.add(textLabel(verdict,"audit-cell"),3,row);row++;
        }
        summary.getChildren().add(audit);view.getChildren().add(summary);
        Label resultText=new Label("Run dental comparison to see agreements and discrepancies.");resultText.getStyleClass().add("result-text");resultText.setWrapText(true);
        Button run=action("Compare synthetic dental charts");run.setOnAction(e->{var result=ForensicComparison.dental(current.dentalReference(),current.dentalRecovered());resultText.setText("RESULT  ·  "+result.finding()+"\n"+result.explanation()+"\n\n"+String.join("  •  ",result.observations()));});view.getChildren().addAll(run,resultText,disclaimer());return view;
    }
    private VBox dentalChart(Dental chart,Label details){VBox box=new VBox(12);box.getStyleClass().add("card");box.setPrefWidth(345);Label label=section(chart.name().toUpperCase());box.getChildren().add(label);GridPane grid=new GridPane();grid.setHgap(6);grid.setVgap(7);
        int[] ids={11,12,13,14,15,16,17,18,21,22,23,24,25,26,27,28};
        for(int i=0;i<ids.length;i++){
            int id=ids[i];String condition=chart.chart().get(id);Button tooth=new Button(id+"\n"+(condition==null?"—":condition));tooth.getStyleClass().add("tooth");tooth.setMinSize(73,54);tooth.setOnAction(e->details.setText(chart.name()+" · Tooth "+id+" · "+(condition==null?"Not recorded":condition)));grid.add(tooth,i%4,i/4);
        }
        box.getChildren().add(grid);return box;
    }
    private Label disclaimer(){Label d=new Label("EDUCATIONAL SIMULATION ONLY · Feature locations, quality ratings and dental charts are fabricated. Results are not medically or forensically validated and must not be used to establish identity.");d.setWrapText(true);d.getStyleClass().add("muted");return d;}
    private void renderTimeline(VBox out){
        out.getChildren().add(textLabel("SUBJECT  ·  "+profile().name()+"  /  "+profile().missingFor()+" MISSING  /  "+profile().location(),"eyebrow"));
        out.getChildren().add(textLabel("CASE EVIDENCE REGISTER  /  All entries are fabricated for training. Record-review checkboxes modify only the local demonstration state.","lead"));
        VBox ledger=panel("Evidence ledger", "Review source, timestamp, and status. Search the listed events to narrow the display.");
        TextField search=new TextField();search.setPromptText("Search events, source, or status...");search.getStyleClass().add("search-field");ledger.getChildren().add(search);
        VBox records=new VBox(9);ledger.getChildren().add(records);
        Runnable update=()->{
            records.getChildren().clear();String q=search.getText().strip().toLowerCase(Locale.ROOT);
            for(String event:details().events()){
                String[] p=event.split("\\|");String line=String.join(" ",p).toLowerCase(Locale.ROOT);
                if(!line.contains(q))continue;
                String key=current.id()+"-"+p[0];
                CheckBox reviewed=new CheckBox("Reviewed");reviewed.setSelected(reviewedEvidence.contains(key));
                Label stamp=textLabel(p[0],"event-time");stamp.setMinWidth(68);
                VBox body=new VBox(3,textLabel(p[1],"row-heading"),textLabel("SOURCE  "+p[2]+"    •    ORIGINAL STATUS  "+p[3],"muted"));
                HBox.setHgrow(body,Priority.ALWAYS);
                HBox lineRow=new HBox(15,stamp,body,reviewed);lineRow.setAlignment(Pos.CENTER_LEFT);lineRow.getStyleClass().add("ledger-row");
                reviewed.setOnAction(e->{if(reviewed.isSelected())reviewedEvidence.add(key);else reviewedEvidence.remove(key);});records.getChildren().add(lineRow);
            }
            if(records.getChildren().isEmpty())records.getChildren().add(textLabel("No entries match that search.","muted"));
        };
        search.textProperty().addListener((obs,o,n)->update.run());update.run();out.getChildren().add(ledger);
        VBox integrity=panel("Data integrity & scope", "This training dataset contains fictional case records. Checkbox states are temporary; no actual chain-of-custody log, access control, examiner approval, or forensic verification is provided.");out.getChildren().add(integrity);
    }
    private VBox panel(String title,String description){VBox p=new VBox(12);p.getStyleClass().add("card");Label h=new Label(title);h.getStyleClass().add("card-title");Label d=new Label(description);d.setWrapText(true);d.getStyleClass().add("muted");p.getChildren().addAll(h,d);return p;}
    private Label section(String t){Label l=new Label(t);l.getStyleClass().add("eyebrow");return l;}
    private Button action(String t){Button b=new Button(t);b.getStyleClass().add("primary-button");return b;}
    public static void main(String[] args){launch(args);}
}
