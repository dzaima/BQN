package APL.tokenizer;

import APL.errors.*;
import APL.tokenizer.types.*;
import APL.tools.Format;
import APL.types.*;

import java.math.BigInteger;
import java.util.*;

// TODO try storing current index in the Tk2
public class Tk2 {
  public static final String surrogateOps = "𝕩𝕏𝕨𝕎𝕗𝔽𝕘𝔾𝕤𝕊𝕣ℝ";
  
  
  public final String r;
  public final Value[] args;
  public Tk2(String r, Value[] args) {
    this.args = args;
    this.r = r;
  }
  
  
  static class TkRes<T> {
    public final ArrayList<T> ct;
    public final int e; // after end
    public TkRes(ArrayList<T> ct, int e) {
      this.ct = ct;
      this.e = e;
    }
  }
  
  
  
  public BasicLines tkBlock() {
    TkRes<Token> aa = tkBlock(0);
    if (aa.e < r.length()) unmatched(aa.e);
    return new BasicLines(r, 0, r.length(), aa.ct);
  }
  public ArrayList<Token> tkLine() {
    TkRes<Token> aa = tkLine(0);
    if (aa.e < r.length()) err("Expected single statement", aa.e);
    return aa.ct;
  }
  
  
  
  
  // TkRes<Token> tkBlock(int i) {
  //   ArrayList<Token> lns = new ArrayList<>();
  //   char c = r.charAt(i);
  //   while (c!=')' & c!='⟩' & c!='}') {
  //     TkRes<Token> ct = tkLine(i);
  //     if (ct.ct.size()!=0) lns.add(new LineTok(r, i, ct.e+1, ct.ct));
  //     i = ct.e;
  //     if (i>=r.length()) break;
  //     c = r.charAt(i);
  //     if (c=='\n' | c=='\r' | c=='⋄' | c==',') i++;
  //     else if (c==';') lns.add(lnWrap(new SemiTok (r, i, ++i)));
  //     else if (c==':') lns.add(lnWrap(new ColonTok(r, i, ++i)));
  //   }
  //   return new TkRes<>(lns, i);
  // }
  
  TkRes<Token> tkBlock(int i) {
    ArrayList<Token> lns = new ArrayList<>();
    while (true) {
      TkRes<Token> ct = tkLine(i);
      if (!ct.ct.isEmpty()) lns.add(new LineTok(r, i, ct.e+1, ct.ct));
      i = ct.e;
      if (i>=r.length()) break;
      char c = r.charAt(i);
      if (c==')' | c=='⟩' | c=='}') break;
      else if (c==';') lns.add(lnWrap(new SemiTok (r, i, ++i)));
      else if (c==':') lns.add(lnWrap(new ColonTok(r, i, ++i)));
      else i++; // skip [\n\r⋄,]
    }
    return new TkRes<>(lns, i);
  }
  
  
  LineTok lnWrap(Token t) {
    ArrayList<Token> ts = new ArrayList<>();
    ts.add(t);
    return new LineTok(t.raw, t.spos, t.epos, ts);
  }
  
  @SuppressWarnings("TextLabelInSwitchStatement") // aaa
  TkRes<Token> tkLine(int s) { // will end at [\n\r,⋄] [;:] [)⟩}]
    String r = this.r;
    ArrayList<Token> res = new ArrayList<>(16);
    int li, i=s;
    boolean hasStrands = false;
    loop: while (i < r.length()) {
      li = i;
      char c = r.charAt(i);
      char n = i+1<r.length()? r.charAt(i+1) : 0;
      switch (c) {
        // generic syntax & exit
        case ' ': case '\t':
          i++;
          break;
        case '(': case '⟨': case '{':
          TkRes<Token> ct = tkBlock(i+1);
          i = ct.e;
          if (i>=r.length()) unmatched(ct.e);
          char cl = r.charAt(i++);
          Token t;
          switch (c) {
            case '(': if (cl!=')') err("Mismatched parentheses", li, i);
              if (ct.ct.size()!=1) err("Parentheses cannot contain "+(ct.ct.isEmpty()?"no":"multiple")+" statements", li, i);
              t = new ParenTok(r, li, i, ct.ct.get(0));
              break;
            case '⟨': if (cl!='⟩') err("Mismatched parentheses", li, i);
              t = new ArrayTok(r, li, i, ct.ct);
              break;
            case '{': if (cl!='}') err("Mismatched parentheses", li, i);
              t = new BlockTok(r, li, i, ct.ct);
              // t = new ArrayTok(r, li, i, ct.ct);
              break;
            default: throw new IllegalStateException();
          }
          res.add(t);
          break;
        case '\n': case '\r': case ',': case '⋄':
        case ';': case ':':
        case ')': case '⟩': case '}':
          break loop;
        case '#':
          while (i<r.length() && r.charAt(i)!='\n') i++;
          break loop; // !!
        
        // single-char special tokens
        case '←': res.add(new SetTok     (r, li, ++i)); break;
        case '⇐': res.add(new ExportTok  (r, li, ++i)); break;
        case '↩': res.add(new ModTok     (r, li, ++i)); break;
        case '@': res.add(new NullChrTok (r, li, ++i)); break;
        case '·': res.add(new NothingTok (r, li, ++i)); break;
        case '‿': res.add(new StranderTok(r, li, ++i)); hasStrands = true; break;
        
        // double-struck chars
        case 55349:
          if (i+1>=r.length()) err("Unfinished surrogate pair \\uD835", li);
          if (surrogateOps.indexOf(n) == -1) {
            err("Unknown token `\uD835"+n+"` (\\uD835\\u"+hex4(n)+")", li);
          }
          i+= 2;
          res.add(new OpTok(r, li, i, r.substring(li, i)));
          break;
  
  
        // case'+':case'-':case'×':case'÷':case'*':case'⋆':case'√':case'⌊':case'⌈':case'∧':case'∨':case'¬':case'|':case'=':case'≠':case'≤':case'<':case'>':case'≥':case'≡':case'≢':case'⊣':case'⊢':case'⥊':case'∾':case'≍':case'↑':case'↓':case'↕':case'⌽':case'⍉':case'/':case'⍋':case'⍒':case'⊏':case'⊑':case'⊐':case'⊒':case'∊':case'⍷':case'⊔':case'⍎':case'⍕':case'!':case'»':case'«':
        // case'∘':case'⊸':case'⟜':case'○':case'⌾':case'⎉':case'⚇':case'⍟':case'⊘':case'◶':case'⎊':
        // case'˜':case'˘':case'¨':case'⌜':case'⁼':case'´':case'`':case'˝':case'˙':
        //   res.add(new OpTok(r, li, ++i, String.valueOf(c)));
        //   break;
        
        
        // numbers
        case'.': // but first, try to create a namespace dot token
          if (n<'0' | n>'9') {
            res.add(new DotTok(r, li, ++i));
            break;
          }
          /* else fallthrough; n∊0…9 */
        case'0':case'1':case'2':case'3':case'4':case'5':case'6':case'7':case'8':case'9':
        case'¯':case'π':case'∞':
          // if (c=='.' && n<'0' | n>'9') {
          //   res.add(new DotTok(r, li, ++i));
          //   break;
          // }
          boolean neg = c=='¯';
          if (neg) {
            if(++i>=r.length()) err("Unfinished number", li, i);
            c = r.charAt(i);
          }
          if (c=='π' | c=='∞') { // handle special constants
            double v = c == 'π'? Math.PI : Double.POSITIVE_INFINITY;
            res.add(new NumTok(r, li, ++i, neg? -v : v));
            break;
          }
          int numStart = i;
          while (c>='0' && c<='9') { // regular integer part
            if (++i>=r.length()) break; c = r.charAt(i);
          }
          dot: if (c=='.') { // fractional part
            if (++i>=r.length()) break dot; c = r.charAt(i); // skip '.'
            while (c>='0' && c<='9') {                       // skip digits
              if (++i>=r.length()) break; c = r.charAt(i);
            }
          } else if (c=='L') { // bigint
            BigInteger v = new BigInteger(r.substring(numStart, i));
            ++i;
            res.add(new BigTok(r, li, i, new BigValue(neg? v.negate() : v)));
            break;
          }
          if (c=='e' | c=='E') { // exponent
            if(         ++i>=r.length()) err("Unfinished number",li,i); c = r.charAt(i); // skip e/E
            boolean eNeg = c=='¯'; int eNegPos = i-li;
            if (eNeg && ++i>=r.length()) err("Unfinished number",li,i); c = r.charAt(i); // skip ¯ if needed
            if (c<'0' | c>'9')           err("Unfinished number",li,i); i++; // make sure there's at least one exponent digit and skip it
            while (i<r.length() && (c=r.charAt(i))>='0' & c<='9') i++;
            double v;
            if (eNeg) {
              char[] cs = new char[i-numStart]; r.getChars(numStart, i, cs, 0);
              cs[eNegPos] = '-';
              v = Double.parseDouble(new String(cs));
            } else {
              v = Double.parseDouble(r.substring(numStart, i));
            }
            res.add(new NumTok(r, li, i, neg? -v : v));
          } else { // no exponent, simple
            double v = Double.parseDouble(r.substring(numStart, i));
            res.add(new NumTok(r, li, i, neg? -v : v));
          }
          break;
        
        // character literal
        case '\'':
          i+= 3;
          if (i>r.length()) err("Unfinished character literal", li, i);
          res.add(new ChrTok(r, li, i, r.charAt(li+1)));
          break;
        // string literal
        case '"':
          i++;
          StringBuilder b = new StringBuilder();
          while (true) {
            if (i>=r.length()) err("Unfinished string", li, r.length());
            c = r.charAt(i);
            if (c=='"') {
              if (++i>=r.length() || r.charAt(i)!='"') break;
            }
            b.append(c);
            i++;
          }
          res.add(new StrTok(r, li, i, b.toString()));
          break;
        // backslash character literal
        case '\\':
          if (n==' ') throw new DomainError("Unfinished backslash literal");
          c = n;
          i+= 2;
          int chr;
          if (c=='0') chr = '\0';
          else if (c=='n') chr = '\n';
          else if (c=='r') chr = '\r';
          else if (c=='t') chr = '\t';
          else if (c=='x' || c=='u') {
            chr = 0;
            i--;
            while (++i<r.length()) {
              c = r.charAt(i);
              if (c>='0'&&c<='9') chr = chr*16 + c-'0';
              else if (c>='a'&&c<='f' || c>='A'&&c<='F') chr = chr*16 + 10+(c-'a' & 31);
              else break;
            }
            if (li+2==i) err("Empty \\"+n+" escape", li, i);
          } else { err("Unrecognized backslash sequence: '\\"+c+"'", li, i); break; }
          res.add(new ChrTok(r, li, i, Format.chr(chr)));
          break;
        
        // names
        // case'A':case'B':case'C':case'D':case'E':case'F':case'G':case'H':case'I':case'J':case'K':case'L':case'M':case'N':case'O':case'P':case'Q':case'R':case'S':case'T':case'U':case'V':case'W':case'X':case'Y':case'Z':
        // case'a':case'b':case'c':case'd':case'e':case'f':case'g':case'h':case'i':case'j':case'k':case'l':case'm':case'n':case'o':case'p':case'q':case'r':case's':case't':case'u':case'v':case'w':case'x':case'y':case'z':
        // case'•':case'_':
        //   i++;
        //   if (c=='_' && n=='\uD835') { // "𝕣".charAt(0/1↓)
        //     if (i+2>=r.length() || r.charAt(i+1)!='\uDD63') err("Invalid name", li, r.length());
        //     boolean md2 = i+3<r.length() && r.charAt(i+2)=='_';
        //     i+= md2? 3 : 2;
        //     res.add(new NameTok(r, li, i, md2? "_𝕣_" : "_𝕣", args));
        //     break;
        //   }
        //   while (i<r.length() && validNameMid(r.charAt(i))) i++;
        //   if (c=='•' && li+1==i) res.add(new OpTok(r, li, i, "•"));
        //   else res.add(new NameTok(r, li, i, r.substring(li, i), args));
        //   break;
        default:
          if ((char)(c-'a')<=('z'-'a') | (char)(c-'A')<=('Z'-'A') | c=='_' | c=='•') {
            i++;
            if (c=='_' && n=='\uD835') { // "𝕣".charAt(0/1↓)
              if (i+2>=r.length() || r.charAt(i+1)!='\uDD63') err("Invalid name", li, r.length());
              boolean md2 = i+3<r.length() && r.charAt(i+2)=='_';
              i+= md2? 3 : 2;
              res.add(new NameTok(r, li, i, md2? "_𝕣_" : "_𝕣", args));
              break;
            }
            while (i<r.length() && validNameMid(r.charAt(i))) i++;
            if (c=='•' && li+1==i) res.add(new OpTok(r, li, i, "•"));
            else res.add(new NameTok(r, li, i, r.substring(li, i), args));
          } else {
            res.add(new OpTok(r, li, ++i, String.valueOf(c)));
          }
          break;
        // err("Unknown token `"+c+"` (\\u"+hex4(c)+")", li);
      }
    }
    if (hasStrands) {
      ArrayList<Token> stranded = new ArrayList<>(res.size()-2);
      int done = 0;
      for (int ss = 0; ss < res.size(); ss++) {
        if (res.get(ss) instanceof StranderTok) {
          int se = ss; // strand end, strand start
          while (se<res.size() && res.get(se) instanceof StranderTok) {
            if (se==0 || se==res.size()-1) err("Expression can't "+(se==0?"start":"end")+" with `‿`", res.get(se).spos);
            se+= 2;
          }
          ArrayList<Token> arr = new ArrayList<>();
          for (int j = ss-1; j <= se-1; j+= 2) {
            Token e = res.get(j);
            if (e instanceof StranderTok) err("Didn't expect `‿`", e.spos);
            arr.add(e);
          }
          stranded.addAll(res.subList(done, ss-1));
          stranded.add(new StrandTok(r, arr.get(0).spos, arr.get(arr.size()-1).epos, arr));
          done = ss = se;
        }
      }
      stranded.addAll(res.subList(done, res.size()));
      res = stranded;
    }
    return new TkRes<>(res, i);
  }
  
  private static boolean validNameMid(char c) {
    return (char)(c-'a')<=('z'-'a') || (char)(c-'A')<=('Z'-'A') | ((char)(c-'0')<=('9'-'0')) | c=='_';
  }
  
  private void err(String msg, int pos) {
    throw new SyntaxError(msg, new ErrTok(r, pos>=r.length()? r.length()-1 : pos));
  }
  private void err(String msg, int spos, int epos) {
    throw new SyntaxError(msg, new ErrTok(r, spos, epos));
  }
  private void unmatched(int pos) { err("Unmatched parentheses", pos); }
  
  
  public static String hex4(int i) {
    String t = Integer.toHexString(i);
    StringBuilder b = new StringBuilder();
    for (int j = 0; j < 4-t.length(); j++) b.append(' ');
    b.append(t);
    return b.toString();
  }
}
