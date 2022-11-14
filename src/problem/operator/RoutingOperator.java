package problem.operator;

import metaheurictics.strategy.Strategy;
import problem.Definition;
import problem.definition.Operator;
import problem.definition.State;

import java.util.*;

public class RoutingOperator extends Operator {

    /**
     * Constructor por defecto
     */
    public RoutingOperator() {
    }

    /**
     * Define la estrategia para la construccion de soluciones iniciales del problema
     * Se utiliza la heuristica del vecino mas cercano para la construccion de la solucion
     * inicial.
     * Genera una lista de soluciones iniciales para el problema con el tamanno de vecindad
     * especificado.
     * La vecindad se genera a partir de mutaciones al resultado del vecino mas cercano
     *
     * @param neighbourhoodSize Cantidad de soluciones a generar
     * @return Lista de soluciones
     */
    @Override
    public List<State> generateRandomState(Integer neighbourhoodSize) {
        //Vecindad de soluciones
        List<State> neighbourhood = new ArrayList<>();

        //Crear la solucion inicial
        State initial = new State();
        //Lista para la codificacion de la solucion inicial
        ArrayList<Object> initialCode = new ArrayList<>();
        //Primero se agrega el punto de partida de la ruta
        initialCode.add(0);

        //Inicializar los valores de acuerdo a la heuristica del vecino mas cercano
        for (int j = 1; j < Definition.getDefinition().getAmountDestinations(); j++) {
            //Obtiene los costos de todas las transiciones a partir del destino actual
            float[] costsFrom = Definition.getDefinition().getTransitionsCost()[j].clone();
            //Bandera para indicar insercion
            boolean insert = false;

            //Iterar hasta lograr un destino valido
            do {
                //Indice del destino con menor coste de transicion
                int indexMin = 0;
                float min = costsFrom[0];

                //Obtener el indice del destino con menor coste
                for (int k = 1; k < costsFrom.length; k++) {
                    if (costsFrom[k] < min) {
                        min = costsFrom[k];
                        indexMin = k;
                    }
                }
                //Comprobar que el destino pueda ser el siguiente
                //Un destino puede ser el siguiente si:
                //- No es el origen (indice 0)
                //- No ha sido visitado (no se encuentra en initialCode)
                //- No es priorizado pero ya no quedan priorizados por visitar o es priorizado
                if (indexMin != 0 && (j > Definition.getDefinition().getAmountPrioritized() ||
                        Definition.getDefinition().isPrioritized(indexMin)) && !initialCode.contains(indexMin)) {
                    //Insercion exitosa
                    initialCode.add(indexMin);
                    insert = true;
                }
                else {
                    //Se descarta este destino
                    costsFrom[indexMin] = Float.MAX_VALUE;
                }
            } while (!insert);
        }
        //Asignar la codificacion a la solucion inicial
        initial.setCode(initialCode);
        //Agregar la nueva solucion a la lista de soluciones vecinas
        neighbourhood.add(initial);
        //Generar una vecindad de soluciones alrededor de la solucion inicial
        neighbourhood.addAll(generatedNewState(initial, neighbourhoodSize - 1));
        //Devolver la vecindad generada
        return neighbourhood;
    }

    /**
     * Define la estructura de vecindades del problema.
     * Genera una lista de soluciones vecinas a la solución recibida con el tamanno recibido.
     * La generacion se realiza mediante la heuristica del intercambio simple aleatorio
     *
     * @param state             solucion de referencia a partir de la cual se obtendran nuevas soluciones vecinas a ella.
     * @param neighbourhoodSize Cantidad de soluciones vecinas a generar
     * @return Lista de soluciones vecinas.
     */
    @Override
    public List<State> generatedNewState(State state, Integer neighbourhoodSize) {
        //Lista de vecindades
        List<State> neighbourhood = new ArrayList<>();
        //Obtener el generador aleatorio definido para el problema
        Random randomGenerator = Definition.getDefinition().getRandomGenerator();
        //Obtener la cantidad de productos priorizados
        int amPrioritized = Definition.getDefinition().getAmountPrioritized();

        //Se generan tantas soluciones como las especificadas por el tamanno de la vecindad
        for (int i = 0; i < neighbourhoodSize; i++) {
            //Instancia para la solucion vecina
            State neighbour;
            //Instancia para la codificacion de la solucion vecina
            ArrayList<Object> neighbourCode;
            //Bandera que indica un intercambio valido
            boolean correctExchange = false;

            neighbour = new State();
            neighbourCode = new ArrayList<>(state.getCode());

            //Obtener las posiciones a intercambiar
            int dest0;
            int dest1;

            do {
                //Comprobar que no se seleccione el origen para intercambiar
                dest0 = randomGenerator.nextInt(Definition.getDefinition().getAmountDestinations());
            } while (dest0 == 0);

            do {
                dest1 = randomGenerator.nextInt(Definition.getDefinition().getAmountDestinations());

                //Comprobar que no se obtenga el mismo indice para la segunda posicion
                if (dest1 != 0 && dest1 != dest0) {
                    //Comprobar que se intercambien solo productos priorizados con priorizados y no
                    //priorizados con no priorizados
                    if (dest0 >= amPrioritized && dest1 >= amPrioritized ||
                        dest0 < amPrioritized && dest1 < amPrioritized) {
                        correctExchange = true;
                    }
                }
            } while (!correctExchange);

            //Se obtienen los destinos que fueron visitados en las posiciones obtenidas de la ruta
            int atPos0 = (int) neighbourCode.get(dest0);
            int atPos1 = (int) neighbourCode.get(dest1);

            //Intercambio de las posiciones de los destinos en el recorrido
            neighbourCode.set(dest1, atPos0);
            neighbourCode.set(dest0, atPos1);

            //Asignar los nuevos valores de las variables de decision a la solucion vecina
            neighbour.setCode(neighbourCode);

            //Agregar la nueva solucion a la lista de soluciones vecinas
            neighbourhood.add(neighbour);
        }
        //Devolver la vecindad generada
        return neighbourhood;
    }

    /**
     * Realiza el cruzamiento de dos padres para un algoritmo genetico
     * Obtiene un nuevo individuo a partir de la mezcla del recorrido de productos
     * priorizados de un padre con el recorrido del resto de productos del otro
     *
     * @param father0 Solucion padre
     * @param father1 Solucion padre
     * @return Solucion derivada de ambos padres
     */
    @Override
    public List<State> generateNewStateByCrossover(State father0, State father1) {
        //Nueva solucion que sera derivada de ambos padres
        State state = new State(new ArrayList<>());
        //Obtener el generador aleatorio definido para el problema
        Random randomGenerator = Definition.getDefinition().getRandomGenerator();
        //Codificacion de la solucion father0
        List<Object> codeF0 = father0.getCode();
        //Codificacion de la solucion father1
        List<Object> codeF1 = father1.getCode();
        //Determinar punto de division de los cromosomas
        int splitPoint;

        do {
            //Evitar puntos muy extremos para dividir los estados
            splitPoint = randomGenerator.nextInt(codeF0.size());
        }
        while (splitPoint == 0 || splitPoint == codeF0.size() - 1);

        //Agregar la primera parte de la codificacion de father0 a la nueva solucion
        state.getCode().addAll(codeF0.subList(0, splitPoint));
        //Agregar la segunda parte de la codificacion de father1 a la nueva solucion
        state.getCode().addAll(codeF1.subList(splitPoint, codeF1.size()));
        //Tratamiento de la solucion generada segun su factibilidad
        State finalSolution = feasibilityTreatment(state, father0, father1);
        //Obtiene la solucion
        return Collections.singletonList(finalSolution);
    }

    /**
     * Tratamiento de la factibilidad de una solucion
     * Las soluciones no factibles son rechazadas y se toma a uno de sus padres
     * aleatoriamente
     *
     * @param crossedSolution solucion generada
     * @param father0         primer padre de la solucion
     * @param father1         segundo padre de la solucion
     * @return Solucion seleccionada segun la factibilidad
     */
    private State feasibilityTreatment(State crossedSolution, State father0, State father1) {
        //Evalua la factibilidad de la solucion
        if (!Strategy.getStrategy().getProblem().getCodification().validState(crossedSolution)) {
            //Si la solucion no es factible devuelve aleatoriamente a uno de sus padres
            return Definition.getDefinition().getRandomGenerator().nextBoolean() ? father0 : father1;
        }
        return crossedSolution;
    }
}
