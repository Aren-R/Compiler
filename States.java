import java.util.Map;

public class States {
    public State root;
    public State currentState;

    public States() {
        //create states here
    }

    public class State {
        public String name;
        public boolean isAccepting;
        public Map<Character, State> transitions;

        public State(String name, Map<Character, State> transitions) {
            this.name = name;
        }

    }
}