import java.util.HashMap;
import java.util.Map;

public class States {
    public State root;
    public State currentState;
    public State [] states;

    public void resetDFA() {
        currentState = root;
    }

    public State transition(String c) {
        // Check for any whitespace character (space, tab, newline, carriage return)
        if (!(c.equals(" ") || c.equals("\t") || c.equals("\n") || c.equals("\r"))) {
            State nextState = currentState.getNextState(c);
            currentState = nextState;
            return nextState;
        } else {
            // For any whitespace, reset the DFA but return the old state
            State oldState = currentState;
            resetDFA();
            return oldState;
        }
    }
    


    //Create all DFA states here
    public States() {


        this.states = new State[109];
        for (int x = 0; x < states.length; x++) {
            states[x] = new State();
        }

        //set root
        root = states[0];
        currentState = root;

        //RESERVED WORDS===========================================================

        states[0].transitions.put("m", states[1]); //m
        states[1].transitions.put("a", states[3]); //ma
        states[1].transitions.put("u", states[2]); //mu
        states[2].transitions.put("l", states[6]); //mul
        states[6].isAccepting = true; //mul
        states[6].classType = "reserved_word";

        states[3].transitions.put("i", states[4]); //mai
        states[4].transitions.put("n", states[5]); //main
        states[5].isAccepting = true; //main
        states[5].classType = "reserved_word";

        states[0].transitions.put("g", states[71]); //g
        states[71].transitions.put("r", states[72]); //gr
        states[72].transitions.put("t", states[73]); //grt
        states[73].isAccepting = true; //grt
        states[73].classType = "reserved_word";

        states[0].transitions.put("d", states[74]); //d
        states[74].transitions.put("i", states[75]); //di
        states[75].transitions.put("v", states[76]); //div
        states[76].isAccepting = true; //div
        states[76].classType = "reserved_word";

        states[0].transitions.put("v", states[77]); //v
        states[77].transitions.put("o", states[78]); //vo
        states[78].transitions.put("i", states[79]); //voi
        states[79].transitions.put("d", states[80]); //void
        states[80].isAccepting = true; //void
        states[80].classType = "reserved_word";

        states[0].transitions.put("p", states[51]); //p
        states[51].transitions.put("r", states[52]); //pr
        states[52].transitions.put("i", states[53]); //pri
        states[53].transitions.put("n", states[54]); //prin
        states[54].transitions.put("t", states[55]); //print
        states[55].isAccepting = true; //print
        states[55].classType = "reserved_word";

        states[0].transitions.put("r", states[81]); //r
        states[81].transitions.put("e", states[82]); //re
        states[82].transitions.put("t", states[83]); //ret
        states[83].transitions.put("u", states[84]); //retu
        states[84].transitions.put("r", states[85]); //retur
        states[85].transitions.put("n", states[86]); //return
        states[86].isAccepting = true; //return
        states[86].classType = "reserved_word";

        states[0].transitions.put(",", states[7]); //,
        states[7].isAccepting = true; //,
        states[7].classType = "reserved_word";
        states[0].transitions.put(";", states[8]); //;
        states[8].isAccepting = true; //;
        states[8].classType = "reserved_word";
        states[0].transitions.put("=", states[9]); //=
        states[9].isAccepting = true; //=
        states[9].classType = "reserved_word";
        states[0].transitions.put("(", states[10]); //(
        states[10].isAccepting = true; //(
        states[10].classType = "reserved_word";
        states[0].transitions.put(")", states[11]); //)
        states[11].isAccepting = true; //)
        states[11].classType = "reserved_word";
        states[0].transitions.put("{", states[12]); //{
        states[12].isAccepting = true; //{
        states[12].classType = "reserved_word";
        states[0].transitions.put("}", states[13]); //}
        states[13].isAccepting = true; //}
        states[13].classType = "reserved_word";

        states[0].transitions.put("n", states[14]); //n
        states[14].transitions.put("u", states[15]); //nu
        states[15].transitions.put("m", states[16]); //num
        states[16].isAccepting = true; //num
        states[16].classType = "reserved_word";
        states[14].transitions.put("o", states[17]); //no
        states[17].transitions.put("t", states[18]); //not
        states[18].isAccepting = true; //not
        states[18].classType = "reserved_word";

        states[0].transitions.put("t", states[19]); //t
        states[19].transitions.put("e", states[20]); //te
        states[20].transitions.put("x", states[21]); //tex
        states[21].transitions.put("t", states[22]); //text
        states[22].isAccepting = true; //text
        states[22].classType = "reserved_word";
        states[19].transitions.put("h", states[23]); //th
        states[23].transitions.put("e", states[24]); //the
        states[24].transitions.put("n", states[25]); //then
        states[25].isAccepting = true; //then
        states[25].classType = "reserved_word";

        states[0].transitions.put("b", states[26]); //b
        states[26].transitions.put("e", states[27]); //be
        states[27].transitions.put("g", states[28]); //beg
        states[28].transitions.put("i", states[29]); //begi
        states[29].transitions.put("n", states[30]); //begin
        states[30].isAccepting = true; //begin
        states[30].classType = "reserved_word";

        states[0].transitions.put("e", states[31]); //e
        states[31].transitions.put("n", states[32]); //en
        states[32].transitions.put("d", states[33]); //end
        states[33].isAccepting = true; //end
        states[33].classType = "reserved_word";
        states[31].transitions.put("l", states[34]); //el
        states[34].transitions.put("s", states[35]); //els
        states[35].transitions.put("e", states[36]); //else
        states[36].isAccepting = true; //else
        states[36].classType = "reserved_word";
        states[31].transitions.put("q", states[37]); //eq
        states[37].isAccepting = true; //eq
        states[37].classType = "reserved_word";

        states[0].transitions.put("s", states[38]); //s
        states[38].transitions.put("q", states[39]); //sq
        states[39].transitions.put("r", states[40]); //sqr
        states[40].transitions.put("t", states[41]); //sqrt
        states[41].isAccepting = true; //sqrt
        states[41].classType = "reserved_word";
        states[38].transitions.put("u", states[42]); //su
        states[42].transitions.put("b", states[43]); //sub
        states[43].isAccepting = true; //sub
        states[43].classType = "reserved_word";
        states[38].transitions.put("k", states[44]); //sk
        states[44].transitions.put("i", states[45]); //ski
        states[45].transitions.put("p", states[46]); //skip
        states[46].isAccepting = true; //skip
        states[46].classType = "reserved_word";

        states[0].transitions.put("h", states[47]); //h
        states[47].transitions.put("a", states[48]); //ha
        states[48].transitions.put("l", states[49]); //hal
        states[49].transitions.put("t", states[50]); //halt
        states[50].isAccepting = true; //halt
        states[50].classType = "reserved_word";

        states[0].transitions.put("i", states[67]); //i
        states[67].transitions.put("f", states[68]); //if
        states[68].isAccepting = true; //if
        states[68].classType = "reserved_word";

        states[0].transitions.put("o", states[69]); //o
        states[69].transitions.put("r", states[70]); //or
        states[70].isAccepting = true; //or
        states[70].classType = "reserved_word";

        states[0].transitions.put("a", states[56]); //a
        states[56].transitions.put("n", states[57]); //an
        states[57].transitions.put("d", states[59]); //and
        states[59].isAccepting = true; //and
        states[59].classType = "reserved_word";
        states[56].transitions.put("d", states[58]); //ad
        states[58].transitions.put("d", states[60]); //add
        states[60].isAccepting = true; //add
        states[60].classType = "reserved_word";

        states[0].transitions.put("<", states[61]); //<
        states[61].transitions.put("i", states[62]); //<i
        states[62].transitions.put("n", states[63]); //<in
        states[63].transitions.put("p", states[64]); //<inp
        states[64].transitions.put("u", states[65]); //<inpu
        states[65].transitions.put("t", states[66]); //<input
        states[66].isAccepting = true; //<input
        states[66].classType = "reserved_word";
        //RESERVED WORDS===========================================================

        //CLASS V==================================================================
        states[0].transitions.put("V", states[87]); //V
        states[87].transitions.put("_", states[88]); //V_
        states[88].transitions.put("abcdefghijklmnopqrstuvwxyz", states[89]); //V_[a-z]
        states[89].transitions.put("abcdefghijklmnopqrstuvwxyz", states[89]);//V_[a-z]([a-z][0-9])*
        states[89].transitions.put("0123456789", states[89]);//V_[a-z]([a-z][0-9])*
        states[89].isAccepting = true; //V_[a-z]([a-z][0-9])*
        states[89].classType = "V";
        //CLASS V==================================================================


        //CLASS F==================================================================
        states[0].transitions.put("F", states[90]); //F
        states[90].transitions.put("_", states[91]); //F_
        states[91].transitions.put("abcdefghijklmnopqrstuvwxyz", states[92]); //F_[a-z]
        states[92].transitions.put("abcdefghijklmnopqrstuvwxyz", states[92]);//F_[a-z]([a-z][0-9])*
        states[92].transitions.put("0123456789", states[92]);//F_[a-z]([a-z][0-9])*
        states[92].isAccepting = true; //F_[a-z]([a-z][0-9])*
        states[92].classType = "F";
        //CLASS F==================================================================


        //CLASS T==================================================================
        states[0].transitions.put("\"", states[93]); //"

        states[93].transitions.put("ABCDEFGHIJKLMNOPQRSTUVWXYZ", states[94]); //"[A-Z]

        states[94].transitions.put("\"", states[107]);//"[A-Z]"
        states[94].transitions.put("abcdefghijklmnopqrstuvwxyz", states[95]); //"[A-Z][a-z]

        states[95].transitions.put("\"", states[107]);//"[A-Z][a-z]"
        states[95].transitions.put("abcdefghijklmnopqrstuvwxyz", states[96]); //"[A-Z][a-z][a-z]
        
        states[96].transitions.put("\"", states[107]);//"[A-Z][a-z][a-z]"
        states[96].transitions.put("abcdefghijklmnopqrstuvwxyz", states[97]); //"[A-Z][a-z][a-z][a-z]
        
        states[97].transitions.put("\"", states[107]);//"[A-Z][a-z][a-z][a-z]"
        states[97].transitions.put("abcdefghijklmnopqrstuvwxyz", states[98]); //"[A-Z][a-z][a-z][a-z][a-z]
        
        states[98].transitions.put("\"", states[107]);//"[A-Z][a-z][a-z][a-z][a-z]"
        states[98].transitions.put("abcdefghijklmnopqrstuvwxyz", states[99]); //"[A-Z][a-z][a-z][a-z][a-z][a-z]

        states[99].transitions.put("\"", states[107]);//"[A-Z][a-z][a-z][a-z][a-z][a-z]"
        states[99].transitions.put("abcdefghijklmnopqrstuvwxyz", states[100]); //"[A-Z][a-z][a-z][a-z][a-z][a-z][a-z]
        
        states[100].transitions.put("\"", states[107]);//"[A-Z][a-z][a-z][a-z][a-z][a-z][a-z]"
        states[100].transitions.put("abcdefghijklmnopqrstuvwxyz", states[108]); //"[A-Z][a-z][a-z][a-z][a-z][a-z][a-z][a-z]
        
        states[108].transitions.put("\"", states[107]);//"[A-Z][a-z][a-z][a-z][a-z][a-z][a-z][a-z]"

        states[107].isAccepting = true; //"[A-Z][a-z]{7}"
        states[107].classType = "T";
        //CLASS T==================================================================

        
        //CLASS N==================================================================
        states[0].transitions.put("123456789", states[101]); //[1-9]
        states[101].isAccepting = true;
        states[101].classType = "N";
        states[101].transitions.put("0123456789", states[101]); //[1-9][0-9]*
        states[101].transitions.put(".", states[102]); //[1-9][0-9]*.

        states[0].transitions.put("0", states[104]); //0
        states[104].isAccepting = true;
        states[104].classType = "N";
        states[104].transitions.put(".", states[102]); //0.

        states[0].transitions.put("-", states[105]); //-
        states[105].transitions.put("123456789", states[101]); //-[1-9]
        states[105].transitions.put("0", states[106]); //-[0]
        states[106].transitions.put(".", states[102]); //-[0].

        states[102].transitions.put("0", states[102]); //.0
        states[102].transitions.put("123456789", states[103]); //.[0-9]*[1-9]
        states[103].transitions.put("0", states[102]); //.[0-9]*[1-9]
        states[103].isAccepting = true;
        states[103].classType = "N";
        //CLASS N==================================================================
    }

    public class State {
        public boolean isAccepting;
        public String classType;
        public Map<String, State> transitions;

        public State() {     
            isAccepting = false;
            transitions = new HashMap<>();
            classType = "NONE";
        }

        public State getNextState(String c) {
            for (String key : transitions.keySet()) {
                if (key.contains(c)) {
                    return transitions.get(key);
                }
            }
            // If no matching transition is found
            State errorState = new State();
            errorState.classType = "Error: Transition not found";
            return errorState;
        }

        public boolean isAccepting() {
            return isAccepting;
        }

        public String getClassType() {
            return classType;
        }

    }
}