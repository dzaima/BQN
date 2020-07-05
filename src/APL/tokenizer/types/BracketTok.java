package APL.tokenizer.types;

import APL.errors.DomainError;

import java.util.List;

public class BracketTok extends TokArr<LineTok> {
  
  public BracketTok(String line, int spos, int epos, List<LineTok> tokens) {
    super(line, spos, epos, tokens);
    if (tokens.size()==0) throw new DomainError("[⋄] is not valid syntax", this);
  }
  
  @Override public String toRepr() {
    StringBuilder s = new StringBuilder("[");
    boolean tail = false;
    for (LineTok v : tokens) {
      if (tail) s.append(" ⋄ ");
      s.append(v.toRepr());
      tail = true;
    }
    s.append("]");
    return s.toString();
  }
  
  public String toString() {
    return "[...]";
  }
}