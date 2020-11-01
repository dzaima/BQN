static class SyntaxHighlight {
  static void apltext(String s, float x, float y, float sz, Theme t, PGraphics g) {
    new SyntaxHighlight(s, t, g).draw(x, y, sz);
  }
  final String s;
  final String[] lns;
  final int[] cs;
  final int[] lnstarts;
  final int[] pairs;
  final int[] mark;
  final Theme th;
  PGraphics g;
  SyntaxHighlight(String s, Theme th, PGraphics g) {
    this.g = g;
    this.th = th;
    this.s = s;
    lns = split(s, "\n");
    lnstarts = new int[lns.length];
    int pos = 0;
    for (int i = 0; i < lns.length; i++) {
      lnstarts[i] = pos;
      pos+= lns[i].length() + 1;
    }
    
    cs = new int[s.length()];
    mark = new int[s.length()];
    pairs = new int[s.length()];
    for (int i = 0; i < s.length(); i++) {
      pairs[i] = -1;
      cs[i] = th.def;
    }
    
    try {
      BasicLines l = Tokenizer.tokenize(s, true, null);
      walk(l, -1);
    } catch(Throwable e) { e.printStackTrace();/* :/ */ } // {[(]j
  }
  
  void draw(float x, float y, float sz) {
    draw(x, y, 0, g.height, sz, -1);
  }
  
  void draw(float x, float y, float sz, int poi) {
    draw(x, y, 0, g.height, sz, poi);
  }
  
  int sel(int poi) {
    if (pairs.length == 0 || poi == -1) return -1;
    else {
      int sel = pairs[min(poi, pairs.length-1)];
      if (sel == -1) sel = pairs[constrain(poi-1, 0, pairs.length-1)];
      return sel;
    }
  }
  
  void draw(float x, float y, float sy, float ey, float sz, int poi) {
    g.textAlign(LEFT, TOP);
    g.textSize(sz);
    int sel = sel(poi);
    float chw = g.textWidth('H');
    for(int ln = max(0, floor((sy-y)/sz)); ln < min(lns.length, floor((ey-y)/sz+1)); ln++) {
      float cx = x;
      String cln = lns[ln];
      for (int i = 0; i < cln.length(); i++) {
        char cc = cln.charAt(i);
        String ccs = Character.toString(cc);
        if (Character.isHighSurrogate(cc)) ccs+= cln.charAt(++i);
        int pos = i + lnstarts[ln];
        g.fill(cs[pos]);
        textS(g, ccs, cx, y + ln*sz);
        int markcol = mark[pos];
        if (markcol != 0) {
          g.fill(markcol);
          textS(g, "_", cx, y + ln*sz);
        }
        if (sel == pos) {
          g.stroke(th.pair);
          g.strokeWeight(ceil(sz/20f));
          g.pushMatrix();
          g.translate(cx, y + ln*sz);
          g.beginShape();
          g.noFill();
          g.vertex(  0, sz* .1);
          g.vertex(  0, sz*1.1);
          g.vertex(chw, sz*1.1);
          g.vertex(chw, sz* .1);
          g.endShape(CLOSE);
          g.popMatrix();
        }
        cx+= chw;
      }
    }
  }
  
  void set(Token t, int col) {
    for (int i = t.spos; i < t.epos; i++) cs[i] = col;
    //println(t.spos+"-"+t.epos+":" + new java.awt.Color(col));
  }
  void set(int i, int col) {
    cs[i] = col;
  }
  void err(int i) {
    mark[i] = th.err;
  }
  void walk(Token t, int dlvl) {
    int dfncol = dlvl<0 || dlvl>=th.dfn.length? th.dfn[0] : th.dfn[dlvl];
    if (t instanceof NumTok) set(t, th.num);
    if (t instanceof BigTok) set(t, th.num);
    if (t instanceof ErrTok) set(t, th.err);
    if (t instanceof StrTok) set(t, th.str);
    if (t instanceof ChrTok) set(t, th.str);
    if (t instanceof SetTok) set(t, th.dmd);
    if (t instanceof ModTok) set(t, th.dmd);
    if (t instanceof ExportTok) set(t, th.dmd);
    if (t instanceof StranderTok) set(t, th.arr);
    
    if (t instanceof CommentTok) set(t, th.com);
    if (t instanceof ColonTok || t instanceof SemiTok) set(t, dfncol);
    if (t instanceof DiamondTok) set(t, th.dmd);
    
    if (t instanceof NameTok) {
      if (t.type=='f') set(t, th.fn);
      if (t.type=='m') set(t, th.mop);
      if (t.type=='d') set(t, th.dop);
    }
    
    if (t instanceof  OpTok) {
      switch(((OpTok) t).op) {
        case "•": set(t, th.def); break;
        case "𝕨": case "𝕎": case "𝕩": case "𝕏":
        case "𝕗": case "𝔽": case "𝕘": case "𝔾":
        case "𝕊":
          set(t, dfncol ); break;
        default: {
          char tp = Comp.typeof(t);
          set(t, tp=='a'||tp=='A'? th.arr : tp=='f'? th.fn : tp=='m'? th.mop : tp=='d'? th.dop : th.err);
        }
      }
    }
    if (t instanceof ParenTok) {
      walk(((ParenTok)t).ln, dlvl);
      if (t.raw.charAt(t.epos-1) != ')') err(t.spos);
      else {
        pairs[t.spos  ] = t.epos-1;
        pairs[t.epos-1] = t.spos  ;
      }
    }
    if (t instanceof BlockTok) {
      int ncol = dlvl+1 >= th.dfn.length? th.dfn[0] : th.dfn[dlvl+1];
      if (t.raw.charAt(t.epos-1) != '}') {
        err(t.spos);
      } else {
        set(t.spos  , ncol);
        set(t.epos-1, ncol);
        pairs[t.spos  ] = t.epos-1;
        pairs[t.epos-1] = t.spos  ;
      }
    }
    if (t instanceof TokArr) {
      int ndlvl = t instanceof BlockTok? dlvl+1 : dlvl;
      for (Token c : ((TokArr)t).tokens) {
        walk(c, ndlvl);
      }
    }
    if (t instanceof ArrayTok) {
      if (t.raw.charAt(t.epos-1) != '⟩') {
        err(t.spos);
      } else {
        set(t.spos  , th.arr);
        set(t.epos-1, th.arr);
        pairs[t.spos  ] = t.epos-1;
        pairs[t.epos-1] = t.spos  ;
      }
    }
  }
}
static class Theme {
  int def = #D2D2D2;
  int lnn = #606060;
  int err = #FF0000;
  int com = #898989;
  int num = #ff6E6E;
  int arr = #DD99FF;
  
  int dmd = #FFFF00;
  int str = #6A9FFB;
  
  int fn  = #57d657;
  int mop = #EB60DB;
  int dop = #FFDD66;
  
  int pair= #777799;
  
  int caret = def;
  int[] dfn = {#AA77BB, #EEBB44, #CC7799, #CCDD00, #B63CDA};
  
}
