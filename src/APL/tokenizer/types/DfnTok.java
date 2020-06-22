package APL.tokenizer.types;

import APL.Comp;
import APL.tokenizer.Token;

import java.util.*;

public class DfnTok extends TokArr<LineTok> {
  public final Comp comp;
  public boolean immediate;
  
  public DfnTok(String line, int spos, int epos, List<LineTok> tokens) {
    super(line, spos, tokens); end(epos);
    type = 'f';
    immediate = true;
    funType(this, this, true);
    comp = Comp.comp(this);
  }
  
  public DfnTok(Comp code, char type) {
    super("•COMP", 0, new ArrayList<>()); end(5);
    immediate = true;
    funType(this, this, true);
    this.type = type;
    comp = code;
  }
  
  public static void funType(Token t, DfnTok dt, boolean first) {
    if (t instanceof TokArr<?>) {
      if (first || !(t instanceof DfnTok)) {
        for (Token c : ((TokArr<?>) t).tokens) funType(c, dt, false);
      }
    } else if (t instanceof OpTok) {
      String op = ((OpTok) t).op;
      if (dt.type != 'd') {
        if (op.equals("𝕗") || op.equals("𝔽")) dt.type = 'm';
        else if (op.equals("𝕘") || op.equals("𝔾")) dt.type = 'd';
      }
      if (dt.immediate) {
        if (op.equals("𝕨") || op.equals("𝕎") || op.equals("𝕩") || op.equals("𝕏")) dt.immediate = false;
      }
    } else if (t instanceof ParenTok) {
      funType(((ParenTok) t).ln, dt, false);
    }
  }
  
  @Override public String toRepr() {
    StringBuilder s = new StringBuilder("{");
    boolean tail = false;
    for (var v : tokens) {
      if (tail) s.append(" ⋄ ");
      s.append(v.toRepr());
      tail = true;
    }
    s.append("}");
    return s.toString();
  }
}