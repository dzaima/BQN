package BQN.tokenizer;

import BQN.errors.*;
import BQN.tokenizer.types.*;
import BQN.tools.Format;
import BQN.types.*;

import java.math.BigInteger;
import java.util.ArrayList;

// TODO try storing current index in the Tk2
public class Tk2 {
  public static final String surrogateOps = "𝕩𝕏𝕨𝕎𝕗𝔽𝕘𝔾𝕤𝕊𝕣ℝ";
  
  
  public final String r;
  private final char[] rC;
  public final Value[] args;
  public Tk2(String r, Value[] args) {
    this.args = args;
    this.r = r;
    rC = new char[r.length()];
    r.getChars(0, r.length(), rC, 0);
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
  
  
  
  
  TkRes<Token> tkBlock(int i) {
    ArrayList<Token> lns = new ArrayList<>();
    while (true) {
      TkRes<Token> ct = tkLine(i);
      if (!ct.ct.isEmpty()) lns.add(new LineTok(r, i, ct.e, ct.ct));
      i = ct.e;
      if (i>=r.length()) break;
      char c = r.charAt(i);
      if (c==')' | c=='⟩' | c=='}') break;
      else if (c==';') lns.add(lnWrap(new SemiTok (r, i, ++i)));
      else if (c==':') lns.add(lnWrap(new ColonTok(r, i, ++i)));
      else if (c=='?') lns.add(lnWrap(new PredTok (r, i, ++i)));
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
    ArrayList<Token> res = new ArrayList<>(16);
    char[] rC = this.rC;
    int len = rC.length;
    int li, i=s;
    boolean hasStrands = false;
    loop: while (i < len) {
      li = i;
      char c = rC[i];
      char n = i+1<len? rC[i+1] : 0;
      switch (c) {
        // generic syntax & exit
        case ' ': case '\t':
          i++;
          break;
        case '(': case '⟨': case '{':
          TkRes<Token> ct = tkBlock(i+1);
          i = ct.e;
          if (i>=len) unmatched(ct.e);
          char cl = rC[i++];
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
        case ';': case ':': case '?':
        case ')': case '⟩': case '}':
          break loop;
        case '#':
          while (i<len && rC[i]!='\n') i++;
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
          if (i+1 >= len) err("Unfinished surrogate pair \\uD835", li);
          if (surrogateOps.indexOf(n) == -1) {
            err("Unknown token `\uD835"+n+"` (\\uD835\\u"+hex4(n)+")", li);
          }
          i+= 2;
          res.add(new OpTok(r, li, i, r.substring(li, i)));
          break;
        
        
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
            if(++i>=len) err("Unfinished number", li, i);
            c = rC[i];
          }
          if (c=='π' | c=='∞') { // handle special constants
            double v = c == 'π'? Math.PI : Double.POSITIVE_INFINITY;
            res.add(new NumTok(r, li, ++i, neg? -v : v));
            break;
          }
          int numStart = i;
          while (c>='0' & c<='9' | c=='_') { // regular integer part
            if (++i>=len) break; c = rC[i];
          }
          dot: if (c=='.') { // fractional part
            if (++i>=len) break dot; c = rC[i]; // skip '.'
            while (c>='0' & c<='9' | c=='_') {  // skip digits
              if (++i>=len) break; c = rC[i];
            }
          } else if (c=='L') { // bigint
            BigInteger v = new BigInteger(r.substring(numStart, i).replace("_", ""));
            ++i;
            res.add(new BigTok(r, li, i, new BigValue(neg? v.negate() : v)));
            break;
          }
          if (c=='e' | c=='E') { // exponent
            if(         ++i>=len) err("Unfinished number",li,i); c = rC[i]; // skip e/E
            boolean eNeg = c=='¯'; int eNegPos = i-li-(neg?1:0);
            if (eNeg && ++i>=len) err("Unfinished number",li,i); c = rC[i]; // skip ¯ if needed
            if (c<'0' | c>'9')           err("Unfinished number",li,i); i++; // make sure there's at least one exponent digit and skip it
            while (i<len && (c=rC[i])>='0' & c<='9' | c=='_') i++;
            double v;
            if (eNeg) {
              char[] cs = new char[i-numStart]; r.getChars(numStart, i, cs, 0);
              cs[eNegPos] = '-';
              v = Double.parseDouble(new String(cs).replace("_", ""));
            } else {
              v = Double.parseDouble(r.substring(numStart, i).replace("_", ""));
            }
            res.add(new NumTok(r, li, i, neg? -v : v));
          } else { // no exponent, simple
            String av = r.substring(numStart, i).replace("_", "");
            try {
              double v = Double.parseDouble(av);
              res.add(new NumTok(r, li, i, neg? -v : v));
            } catch (NumberFormatException e) {
              err("Bad number", li, i);
            }
          }
          break;
        
        // character literal
        case '\'':
          i+= 3;
          if (i>len) err("Unfinished character literal", li, i);
          res.add(new ChrTok(r, li, i, rC[li+1]));
          break;
        // string literal
        case '"':
          i++;
          StringBuilder b = new StringBuilder();
          while (true) {
            if (i>=len) err("Unfinished string", li, len);
            c = rC[i];
            if (c=='"') {
              if (++i>=len || rC[i]!='"') break;
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
            while (++i<len) {
              c = rC[i];
              if (c>='0'&&c<='9') chr = chr*16 + c-'0';
              else if (c>='a'&&c<='f' || c>='A'&&c<='F') chr = chr*16 + 10+(c-'a' & 31);
              else break;
            }
            if (li+2==i) err("Empty \\"+n+" escape", li, i);
          } else { err("Unrecognized backslash sequence: '\\"+c+"'", li, i); break; }
          res.add(new ChrTok(r, li, i, Format.chr(chr)));
          break;
        
        default:
          if ((char)(c-'a')<=('z'-'a') | (char)(c-'A')<=('Z'-'A') | c=='_' | c=='•') {
            i++;
            if (c=='_') {
              if (n=='\uD835') { // "𝕣".charAt(0/1↓)
                if (i+2>=len || rC[i+1]!='\uDD63') err("Invalid name", li, i+1);
                boolean md2 = i+3<len && rC[i+2]=='_';
                i+= md2? 3 : 2;
                res.add(new NameTok(r, li, i, md2? "_𝕣_" : "_𝕣", args));
                break;
              } else if (n>='0' & n<='9') err("Name cannot start with a number", li, i+1);
            }
            while (i<len && validNameMid(rC[i])) i++;
            if (c=='•' && li+1==i) res.add(new OpTok(r, li, i, '•'));
            else res.add(new NameTok(r, li, i, r.substring(li, i), args));
          } else {
            res.add(new OpTok(r, li, ++i, c));
          }
          break;
        // err("Unknown token `"+c+"` (\\u"+hex4(c)+")", li);
      }
    }
    if (hasStrands) {
      if (res.size()==1) throw new SyntaxError("Standalone `‿`");
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
    for (int j = 0; j < 4-t.length(); j++) b.append('0');
    b.append(t);
    return b.toString();
  }
}