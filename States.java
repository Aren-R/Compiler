import java.util.Map;

public class States {
    public State root;
    public State currentState;

    public class State {
        public String name;
        public boolean isAccepting;
        public Map<String, State> transitions;

        public State(String name, Map<String, State> transitions) {
            this.name = name;
            this.transitions = transitions;
        }
    }
}