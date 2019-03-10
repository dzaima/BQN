package APL;

import APL.types.Tokenable;

import java.util.*;


public class Token implements Tokenable { // todo redo in OOP
  public TType type;
  public String repr;
  public List<Token> tokens;
  public String line;
  public int pos;
  Token (TType t, String s, int pos, String line) {
    type = t;
    repr = s;
    this.line = line;
    this.pos = pos;
  }
  Token (TType t, List<Token> s, Token ptr) {
    assert(t==TType.expr||t==TType.lines||t==TType.pick||t==TType.usr);
    type = t;
    tokens = s;
    this.line = s.size()>0? s.get(0).line : ptr.line;
    this.pos = s.size()>0? s.get(0).pos : ptr.pos;
  }
  public Token(TType t, int pos, String line) {
    type = t;
    if (type == TType.set) repr = "←";
    if (type == TType.lines) tokens = new ArrayList<>();
    this.line = line;
    this.pos = pos;
  }
  public String toString() {
    return toRepr();
  }
  
  String toTree(String p) {
    StringBuilder r = new StringBuilder();
    r.append(p).append(type);
    if (repr != null) r.append(": ").append(repr);
    r.append('\n');
    p+= "  ";
    if (tokens != null) {
      for (Token t : tokens) r.append(t.toTree(p));
    }
    return r.toString();
  }
  
  public String toRepr() {
    switch (type) {
      case expr: case pick:
        StringBuilder res = new StringBuilder(type == TType.expr? "(" : "[");
        for (Token t : tokens) {
          if (res.length() > 1) res.append(" ");
          res.append(t.toRepr());
        }
        return res + (type==TType.expr? ")" : "]");
      case usr: case lines:
        res = new StringBuilder(type == TType.usr ? "{" : "(");
        for (Token t : tokens) {
          if (res.length() > 1) res.append(" ⋄ ");
          res.append(t.toRepr());
        }
        return res + (type==TType.usr? "}" : ")");

      case name:  case op: case number: return repr;
      case set: return "←";
      case guard: return ":";
      case errGuard: return "::";
      case chr: return "'" + repr + "'";
      case str: return "\"" + repr + "\"";
      default:
        return type + "";
    }
  }
  private Integer colonPos;
  int colonPos() {
    if (colonPos == null) {
      colonPos = -1;
      for (int i = 0; i < tokens.size(); i++) {
        if (tokens.get(i).type == TType.guard) {
          colonPos = i;
          break;
        }
      }
    }
    return colonPos;
  }
  private Integer eguardPos;
  int eguardPos() {
    if (eguardPos == null) {
      eguardPos = -1;
      for (int i = 0; i < tokens.size(); i++) {
        if (tokens.get(i).type == TType.errGuard) {
          eguardPos = i;
          break;
        }
      }
    }
    return eguardPos;
  }
  
  @Override
  public Token getToken() {
    return this;
  }
}
