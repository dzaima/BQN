package BQN.tokenizer;

import BQN.errors.*;
import BQN.tokenizer.types.*;
import BQN.tools.Format;
import BQN.types.*;

import java.math.BigInteger;
import java.util.*;

public class Tokenizer {
  private static final String ops = "+∘-⊸×⟜÷○*⋆⌾√⎉⌊⚇•⌈⍟∧∨¬|=˜≠˘≤¨<⌜>⁼≥´≡`≢⊣⊢⥊∾≍↑↓↕⌽⍉/⍋⍒⊏⊑⊐⊒∊⍷⊔ℝ⍎⊘◶◴⍕⎊˝˙!»«";
  private static final HashSet<Character> opsHS = new HashSet<>();
  static {
    for (int i = 0; i < ops.length(); i++) {
      opsHS.add(ops.charAt(i));
    }
  }
  public static final String surrogateOps = "𝕩𝕏𝕨𝕎𝕗𝔽𝕘𝔾𝕤𝕊𝕣ℝ";
  private static boolean validNameStart(char c) {
    // for (char l : validNames) if (l == c) return true;
    // return false;
    return c>='a' & c<='z'  |  c>='A' & c<='Z'  |  c=='_';
  }
  public static boolean validNameMid(char c) {
    return validNameStart(c) | c>='0' & c<='9';
  }
  static class Line {
    final ArrayList<Token> ts;
    final String line;
    final int pos;
    
    Line(String line, int pos, ArrayList<Token> ts) {
      this.ts = ts;
      this.line = line;
      this.pos = pos;
    }
    
    Line(String line, int pos) {
      this(line, pos, new ArrayList<>());
    }
    
    public int size() {
      return ts.size();
    }
    
    public void add(Token r) {
      ts.add(r);
    }
    
    LineTok tok(boolean pointless) { // also handles strands because why not
      int spos = size()==0? pos : ts.get(0).spos;
      int epos = size()==0? pos : ts.get(size()-1).epos;
      if (pointless) return new LineTok(line, spos, epos, ts);
      ArrayList<Token> pts = new ArrayList<>();
      
      ArrayList<Token> cstr = new ArrayList<>();
      
      for (int i = 0; i < ts.size(); i++) {
        Token t1 = ts.get(i);
        if (t1 instanceof StranderTok) throw new SyntaxError("Bad strand syntax", t1);
        
        while (i+2<ts.size() && ts.get(i+1) instanceof StranderTok) {
          cstr.add(ts.get(i));
          i+= 2;
        }
        if (cstr.size() > 0) {
          cstr.add(ts.get(i));
          for (Token t : cstr) if (t instanceof StranderTok) throw new SyntaxError("Bad strand syntax", t);
          pts.add(new StrandTok(t1.raw, t1.spos, cstr.get(cstr.size()-1).epos, cstr));
          cstr = new ArrayList<>();
        } else {
          pts.add(t1);
        }
      }
      return new LineTok(line, spos, epos, pts);
    }
  }
  static class Block { // temp storage of multiple lines
    final ArrayList<Line> a;
    final char b;
    private final int pos;
    
    Block(ArrayList<Line> a, char b, int pos) {
      this.a = a;
      this.b = b;
      this.pos = pos;
    }
    public String toString() {
      return "<"+a+","+b+">";
    }
  }
  
  public static BasicLines tokenize(String raw, Value[] args) {
    return tokenize(raw, false, args);
  }
  @SuppressWarnings("StringConcatenationInLoop")
  public static BasicLines tokenize(String raw, boolean pointless, Value[] args) { // pointless means unevaled things get tokens; mainly for syntax highlighting
    int li = 0;
    int len = raw.length();
    
    ArrayList<Block> levels = new ArrayList<>();
    levels.add(new Block(new ArrayList<>(), '⋄', 0));
    levels.get(0).a.add(new Line(raw, 0, new ArrayList<>()));
    
    for (int i = 0; i < len; li = i) {
      Block expr = levels.get(levels.size() - 1);
      ArrayList<Line> lines = expr.a;
      Line tokens = lines.get(lines.size() - 1);
      try {
        char c = raw.charAt(i);
        char next = i+1 < len? raw.charAt(i + 1) : ' ';
        String cS = String.valueOf(c);
        if (c == '(' || c == '{' || c == '[' || c == '⟨') {
          levels.add(new Block(new ArrayList<>(), c, i));
          lines = levels.get(levels.size() - 1).a;
          lines.add(new Line(raw, i));
          
          i++;
        } else if (c == ')' || c == '}' || c == ']' || c == '⟩') {
          char match;
          switch (c) {
            case ')': match = '('; break;
            case '}': match = '{'; break;
            case ']': match = '['; break;
            case '⟩': match = '⟨'; break;
            default:
              throw new Error("this should really not happen");
          }
          Block closed = levels.remove(levels.size() - 1);
          if (match != closed.b) {
            if (pointless) {
              levels.add(closed);
              tokens.add(new ErrTok(raw, i));
              // and leave running for quick exit
            }
            throw new SyntaxError("mismatched parentheses of " + closed.b + " and " + c, new ErrTok(raw, li));
          }
          if (lines.size() > 0 && lines.get(lines.size() - 1).size() == 0) lines.remove(lines.size() - 1); // no trailing empties!!
          
          ArrayList<Token> lineTokens = new ArrayList<>();
          for (Line ta : closed.a) lineTokens.add(ta.tok(pointless));
          Token r;
          switch (c) {
            case ')': if (pointless) { ArrayList<Token> ts = new ArrayList<>(lineTokens); r = new ParenTok(raw, closed.pos, i+1, new LineTok(raw, closed.pos, len, ts)); } else {
              if (lineTokens.size()!=1) throw new SyntaxError("parenthesis should be a single expression", new ErrTok(raw, li));
              r = new ParenTok(raw, closed.pos, i+1, lineTokens.get(0));
            }
            break;
            case '}':
              if (pointless) r = new BlockTok(raw, closed.pos, i+1, lineTokens, true);
              else r = new BlockTok(raw, closed.pos, i+1, lineTokens);
              // else r = new ArrayTok(raw, closed.pos, i+1, lineTokens);
              break;
            case '⟩':
              r = new ArrayTok(raw, closed.pos, i+1, lineTokens);
              break;
            default:
              throw new Error("this should really not happen "+c);
          }
          lines = levels.get(levels.size() - 1).a;
          tokens = lines.get(lines.size() - 1);
          tokens.add(r);
          i++;
        } else if (c=='_' && i+2<len && raw.codePointAt(i+1)=="𝕣".codePointAt(0)) { // +TODO handle more properly, make all 𝕨𝕩𝕎𝕏𝕗𝕘𝔽𝔾𝕤𝕊𝕣ℝ NameToks
          boolean cmp = i+3<len && raw.charAt(i+3) == '_';
          i+= cmp? 4 : 3;
          tokens.add(new NameTok(raw, li, i, cmp? "_𝕣_" : "_𝕣", args));
        } else if (validNameStart(c) || c == '•' && validNameStart(next)) {
          i++;
          while (i < len && validNameMid(raw.charAt(i))) i++;
          String name = raw.substring(li, i);
          tokens.add(new NameTok(raw, li, i, name, args));
        } else if (('∞'==c | 'π'==c)  |  c=='¯' & ('∞'==next | 'π'==next)) {
          boolean neg = c=='¯';
          i+= neg? 2 : 1;
          double v = (neg? next : c) == '∞'? Double.POSITIVE_INFINITY : Math.PI;
          tokens.add(new NumTok(raw, li, i, neg? -v : v));
        } else if (c>='0' && c<='9' || c=='¯' || c=='.' && next>='0' && next<='9') {
          boolean negative = c=='¯';
          if (negative) i++;
          int si = i;
          boolean hasPoint = false;
          while(i < len) {
            c = raw.charAt(i);
            if (hasPoint) {
              if (c<'0' || c>'9') break;
            } else if (c<'0' || c>'9') {
              if (c == '.') hasPoint = true;
              else break;
            }
            i++;
          }
          double f = Double.parseDouble(raw.substring(si,i));
          if (negative) f = -f;
          if (i < len) {
            c = raw.charAt(i);
            boolean hasE = c=='e' | c=='E';
            if (hasE && i+1==len) throw new SyntaxError("unfinished number", new ErrTok(raw, li));
            boolean hasExp = hasE && !validNameStart(raw.charAt(i+1));
            if (hasExp) {
              i++;
              c = raw.charAt(i);
              boolean negExp = c == '¯';
              if (negExp) i++;
              si = i;
              while (i < len) {
                c = raw.charAt(i);
                if (c < '0' || c > '9') break;
                i++;
              }
              int exp = Integer.parseInt(raw.substring(si, i));
              if (negExp) exp = -exp;
              f*= Math.pow(10, exp);
            }
            if (i<len && raw.charAt(i)=='L' && (i+1 == len || !validNameMid(raw.charAt(i+1)))) {
              if (hasExp || hasPoint) {
                if (hasExp) throw new SyntaxError("biginteger literal with exponent", new ErrTok(raw, li));
                throw new SyntaxError("biginteger literal with decimal part", new ErrTok(raw, li));
              }
              i++;
              BigInteger big = new BigInteger(raw.substring(si, i-1));
              if (negative) big = big.negate();
              tokens.add(new BigTok(raw, li, i, new BigValue(big)));
            } else tokens.add(new NumTok(raw, li, i, f));
          } else tokens.add(new NumTok(raw, li, i, f));
        } else if (c == '.') {
          i++;
          tokens.add(new DotTok(raw, li, i));
        } else if (opsHS.contains(c)) {
          i++;
          tokens.add(new OpTok(raw, li, i, cS));
        } else if (c == 55349) { // low surrogate pair of double-struck chars
          if (i+1==len) throw new SyntaxError("expression ended with low surrogate \\uD835", new ErrTok(raw, li));
          if (surrogateOps.indexOf(next) == -1) {
            String hex = Integer.toHexString(next).toUpperCase(); while(hex.length() < 4) hex = "0"+hex;
            throw new SyntaxError("unknown token `\uD835" + next + "` (\\uD835\\u"+hex+")", new ErrTok(raw, li));
          }
          tokens.add(new OpTok(raw, i, i+2, raw.substring(i, i+2)));
          i+= 2;
        } else if (c == '←') {
          tokens.add(new SetTok(raw, i, i+1));
          i++;
        } else if (c == '⇐') {
          tokens.add(new ExportTok(raw, i, i+1));
          i++;
        } else if (c == '↩') {
          tokens.add(new ModTok(raw, i, i+1));
          i++;
        } else if (c == '@') {
          tokens.add(new NullChrTok(raw, i, i+1));
          i++;
        } else if (c == '‿') {
          tokens.add(new StranderTok(raw, i, i+1));
          i++;
        } else if (c == '\\') {
          if (next==' ') throw new DomainError("Unfinished backslash literal");
          c = next;
          i+= 2;
          int chr;
          if (c=='0') chr = '\0';
          else if (c=='n') chr = '\n';
          else if (c=='r') chr = '\r';
          else if (c=='t') chr = '\t';
          else if (c=='x' || c=='u') {
            chr = 0;
            i--;
            while (++i<len) {
              c = raw.charAt(i);
              if (c>='0'&&c<='9') chr = chr*16 + c-'0';
              else if (c>='a'&&c<='f' || c>='A'&&c<='F') chr = chr*16 + 10+(c-'a' & 31);
              else break;
            }
            if (li+2==i) throw new DomainError("Empty \\"+next+" escape", new ErrTok(raw, li, i));
          } else throw new DomainError("Unrecognized backslash sequence: '\\"+c+"'", new ErrTok(raw, li, i));
          tokens.add(new ChrTok(raw, li, i, Format.chr(chr)));
        } else if (c == '\'') {
          if (i+2 >= len) throw new SyntaxError("unfinished character literal", new ErrTok(raw, li));
          if (raw.charAt(i+2) != '\'') {
            throw new SyntaxError("character literal must contain exactly 1"+(Character.isHighSurrogate(raw.charAt(i+1))?" UTF-16":"")+" character", new ErrTok(raw, li));
          }
          i+= 3;
          tokens.add(new ChrTok(raw, li, i, raw.charAt(i-2)+""));
        } else if (c == '"') {
          StringBuilder str = new StringBuilder();
          i++;
          if (i == len) throw new SyntaxError("unfinished string literal", new ErrTok(raw, li));
          while (true) {
            if (raw.charAt(i) == '"') {
              if (i+1==len) break;
              else if (raw.charAt(i+1) != '"') break;
              str.append('"');
              i+= 1;
            } else {
              str.append(raw.charAt(i));
            }
            i++;
            if (i >= len) throw new SyntaxError("unfinished string literal", new ErrTok(raw, li));
          }
          i++;
          tokens.add(new StrTok(raw, li, i, str.toString()));
        } else if (c=='\n' || c=='\r' || c=='⋄' || c==',') {
          if ((c=='⋄' || c==',') && pointless) tokens.add(new DiamondTok(raw, i));
          
          if (tokens.size() > 0) lines.add(new Line(raw, li));
          i++;
        } else if (c==';' || c==':') {
          i++;
          if (tokens.size()==0) throw new SyntaxError("Expected something before "+c, new ErrTok(raw, li));
          Line nln = new Line(raw, li);
          if (c==';') nln.add(new SemiTok (raw, li, i));
          if (c==':') nln.add(new ColonTok(raw, li, i));
          lines.add(nln);
          lines.add(new Line(raw, li));
        } else if (c == '#') {
          i++;
          while (i < len && raw.charAt(i) != '\n') i++;
          if (pointless) tokens.add(new CommentTok(raw, li, i));
        } else if (c == '·') {
          tokens.add(new NothingTok(raw, i, i+1));
          i++;
        } else if (c == ' ' || c == '\t') {i++;} else {
          if (pointless) tokens.add(new ErrTok(raw, i, i+1));
          else {
            String hex = Integer.toHexString(c).toUpperCase(); while(hex.length() < 4) hex = "0"+hex;
            throw new SyntaxError("unknown token `" + c + "` (\\u"+hex+")", new ErrTok(raw, li));
          }
          i++;
        }
        //if (c != ' ') {
        //  printdbg("> "+(c+"").replace("\n","\\n"));
        //  printdbg("curr: "+join(levels, "|"));
        //}
        assert li < i; // error if nothing changed!
      } catch (Throwable e) {
        if (!pointless) throw e;
        
        if (li == i) i = li + 1; // lazy exit out of infinite loops
        tokens.add(new ErrTok(raw, li, i));
      }
    }
    if (levels.size() != 1) {
      if (!pointless) throw new SyntaxError("error matching parentheses", new ErrTok(raw, li)); // or too many
      // else, attempt to recover
      while (levels.size() > 1) {
        Block closed = levels.remove(levels.size() - 1);
        
        ArrayList<Token> lineTokens = new ArrayList<>();
        for (Line ta : closed.a) lineTokens.add(ta.tok(true));
        Token r;
        switch (closed.b) {
          case '(':
            ArrayList<Token> ts = new ArrayList<>(lineTokens);
            r = new ParenTok(raw, closed.pos, len, new LineTok(raw, closed.pos, len, ts));
            break;
          case '{':
            r = new BlockTok(raw, closed.pos, len, lineTokens, true);
            break;
          case '⟨':
            r = new ArrayTok(raw, closed.pos, len, lineTokens);
            break;
          default:
            throw new Error("this should really not happen "+closed.b);
        }
        ArrayList<Line> lines = levels.get(levels.size() - 1).a;
        Line tokens = lines.get(lines.size() - 1);
        tokens.add(r);
      }
    }
    ArrayList<Line> lines = levels.get(0).a;
    if (lines.size() > 0 && lines.get(lines.size()-1).size() == 0) lines.remove(lines.size()-1); // no trailing empties!!
    ArrayList<Token> expressions = new ArrayList<>();
    for (Line line : lines) {
      expressions.add(line.tok(pointless));
    }
    return new BasicLines(raw, 0, len, expressions);
  }
}