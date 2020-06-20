package APL.tokenizer.types;

import APL.Comp;
import APL.tokenizer.Token;

import java.util.List;

public class DfnTok extends TokArr<LineTok> {
  public final Comp comp;
  
  public DfnTok(String line, int spos, int epos, List<LineTok> tokens) {
    super(line, spos, tokens); end(epos);
    type = funType(this, true);
    comp = Comp.comp(this);
  }
  public static char funType(Token t, boolean first) {
    char type = 'f';
    if (t instanceof TokArr<?>) {
      if (first || !(t instanceof DfnTok)) for (Token c : ((TokArr<?>) t).tokens) {
        char n = funType(c, false);
        if (n == 'm') type = 'm';
        else if (n == 'd') return 'd';
      }
    } else if (t instanceof OpTok) {
      String op = ((OpTok) t).op;
      if (op.equals("𝕗") || op.equals("𝔽")) type = 'm';
      else if (op.equals("𝕘") || op.equals("𝔾")) return 'd';
    } else if (t instanceof ParenTok) {
      char n = funType(((ParenTok) t).ln, false);
      if (n == 'm') type = 'm';
      else if (n == 'd') return 'd';
    }
    return type;
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