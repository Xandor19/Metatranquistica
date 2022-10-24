package problem.operator;

import metaheurictics.strategy.Strategy;
import problem.Definition;
import problem.codification.RoutingCodification;
import problem.definition.Operator;
import problem.definition.State;

import java.util.*;

public class RoutingOperator extends Operator {

    private RoutingCodification codif;

    public RoutingOperator() {
        this.codif = new RoutingCodification();
    }

    /**
     * Define la estrategia para la construccion de soluciones iniciales del problema
     * Genera una lista de soluciones iniciales para el problema.
     *
     * @param neighbourhoodSize Cantidad de soluciones a generar
     * @return Lista de soluciones
     */
    @Override
    public List<State> generateRandomState(Integer neighbourhoodSize) {
        //Vecindad de soluciones
        List<State> neighbourhood = new ArrayList<>();

        //Crear una nueva instancia de State que representa la solucion inicial
        State initial = new State();
        //Crear la lista que representa la codificacion de las soluciones
        ArrayList<Object> initialCode = new ArrayList<>();
        //Primero se agrega el punto de partida de la ruta
        initialCode.add(0);

        //Inicializar los valores de acuerdo a la heuristica del vecino mas cercano
        for (int j = 1; j < Definition.getDefinition().getAmountDestinations(); j++) {
            //Obtiene los costos de las transiciones a partir del destino actual
            float[] costsFrom = Definition.getDefinition().getTransitionsCost()[j].clone();
            //Bandera para la insercion
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
        //Asignar los nuevos valores de las variables de decision a la solucion inicial
        initial.setCode(initialCode);
        //Agregar la nueva solucion a la lista de soluciones vecinas
        neighbourhood.add(initial);
        //Generar una vecindad de soluciones
        neighbourhood.addAll(generatedNewState(initial, neighbourhoodSize - 1));

        neighbourhood.forEach(x -> System.out.println(x.getCode().toString()));
        //Devolver la vecindad generada
        return neighbourhood;
    }

    /**
     * Define la estructura de vecindades del problema.
     * Genera una lista de soluciones vecinas a la solución de referencia que se recibe por parametro.
     *
     * @param state             solucion de referencia a partir de la cual se obtendran nuevas soluciones vecinas a ella.
     * @param neighbourhoodSize Cantidad de soluciones vecinas a generar
     * @return Lista de soluciones vecinas.
     */
    @Override
    public List<State> generatedNewState(State state, Integer neighbourhoodSize) {
        List<State> neighbourhood = new ArrayList<>();
        //Generador aleatorio. Usar nextInt() para entero aleatorio, nextDouble() para flotante aleatorio
        Random randomGenerator = Definition.getDefinition().getRandomGenerator();

        //Se generan tantas soluciones como las especificadas por el tamanno de la vecindad
        for (int i = 0; i < neighbourhoodSize; i++) {
            //Crear una nueva instancia de State que representa una solucion vecina
            State neighbour;
            //Crear una copia de las variables de decision de la solucion de referencia para la nueva solucion
            ArrayList<Object> neighbourCode;

            do {
                //Generar soluciones vecinas hasta encontrar una factible
                neighbour = new State();
                neighbourCode = new ArrayList<>(state.getCode());

                //Modificar los valores de las variables de decision de acuerdo a la heuristica definida
                int dest0 = randomGenerator.nextInt(Definition.getDefinition().getAmountDestinations());
                int dest1;

                do {
                    //Comprobar que no se obtenga el mismo indice
                    dest1 = randomGenerator.nextInt(Definition.getDefinition().getAmountDestinations());
                } while (dest0 == dest1);

                //Se obtienen los destinos que fueron visitados en la posicion de la ruta correspondiente
                int atPos0 = (int) neighbourCode.get(dest0);
                int atPos1 = (int) neighbourCode.get(dest1);

                //Intercambio de las posiciones de los destinos en el recorrido
                neighbourCode.set(dest1, atPos0);
                neighbourCode.set(dest0, atPos1);

                //Asignar los nuevos valores de las variables de decision a la solucion vecina
                neighbour.setCode(neighbourCode);

            } while (!codif.validState(neighbour));
            //Agregar la nueva solucion a la lista de soluciones vecinas
            neighbourhood.add(neighbour);
        }
        //Devolver la vecindad generada
        return neighbourhood;
    }

    /**
     * Define la estrategia de cruzamiento para el algoritmo genetico.
     *
     * @param father0 Solucion padre
     * @param father1 Solucion padre
     * @return Solucion derivada de ambos padres
     */
    @Override
    public List<State> generateNewStateByCrossover(State father0, State father1) {
        //Generador de numeros aleatorios
        Random randomGenerator = Definition.getDefinition().getRandomGenerator();
        //Nueva solucion que sera derivada de ambos padres
        State state = new State(new ArrayList<>());
        //Codificacion de la solucion father0
        List<Object> codeF0 = father0.getCode();
        //Codificacion de la solucion father1
        List<Object> codeF1 = father1.getCode();
        //Determinar punto de division de los cromosomas
        int splitPoint;

        //Evitar puntos muy extremos para dividir los cromosomas
        do {
            splitPoint = randomGenerator.nextInt(codeF0.size());
        }
        while (splitPoint == 0 || splitPoint == codeF0.size() - 1);

        //Agregar la primera parte de la codificacion de father0 a la nueva solucion
        state.getCode().addAll(codeF0.subList(0, splitPoint));
        //Agregar la segunda parte de la codificacion de father1 a la nueva solucion
        state.getCode().addAll(codeF1.subList(splitPoint, codeF1.size()));
        //Otras soluciones pueden ser obtenidas intercambiando las partes de las soluciones que se seleccionan
        //Hasta este punto existe la posibilidad de que la solucion obtenida no sea factible.
        //Para solucionar este problema podemos recurrir a varias estrategias:
        //*Rechazo de la solucion: En este caso simplemente devolvemos una de las soluciones padre
        //*Reparacion: Se intenta transformar la solucion obtenida de forma que sea factible y se cumplan las restricciones
        //*Penalizacion: Al evaluar la solucion en la funcion objetivo se puede incrementar el costo de la solucion cuando una variable de decision tome un valor no factible.
        State finalSolution = feasibilityTreatment(state, father0, father1, FeasibilityTreatmentType.REJECT);
        //BiCIAM solo utiliza la primera solucion de la lista asi que no tiene mucho sentido generar otras alternativas de soluciones
        return Collections.singletonList(finalSolution);
    }

    private State feasibilityTreatment(State crossedSolution, State father0, State father1, FeasibilityTreatmentType treatmentType) {
        Random randomGenerator = Definition.getDefinition().getRandomGenerator();
        //Si la solucion obtenida no es factible
        if (!Strategy.getStrategy().getProblem().getCodification().validState(crossedSolution)) {
            switch (treatmentType) {
                //Si la estrategia es rechazar la solucion se devuelve uno de los padres con igual probabilidad para ambos
                case REJECT: {
                    return randomGenerator.nextBoolean() ? father0 : father1;
                }
                //Si la estrategia es penalizar la solucion se devuelve la misma y la no factibilidad se trata al evaluar la funcion objetivo
                case PENALIZATION:
                    return crossedSolution;
                //Intentar reparar la solucion
                default: {
                    //Dada la estrategia de cruzamiento aplicada la pincipal forma de no factibilidad
                    //es que una tarea se encuentre asignada a mas de un trabajador y en consecuencia
                    //otras tareas no hayan sido asignadas
                    ArrayList<Object> codeCS=crossedSolution.getCode();
                    Queue<Integer> unassignedTasks=new ArrayDeque<>();
                    Map<Integer,Integer> taskSeenCount=new HashMap<>();
                    for (int i = 0; i < Definition.getDefinition().getAmountDestinations(); i++) {
                        if(!codeCS.contains(i)){
                            unassignedTasks.add(i);
                        }
                    }
                    for (int i = 0; i < Definition.getDefinition().getAmountDestinations(); i++) {
                        int task= (int) codeCS.get(i);
                        if(!taskSeenCount.containsKey(task)){
                            taskSeenCount.put(task,1);
                        }else{
                            int currentCount=taskSeenCount.get(task);
                            taskSeenCount.put(task,currentCount+1);
                        }
                    }
                    while (!unassignedTasks.isEmpty()){
                        int unassigned= unassignedTasks.poll();
                        for(int key:taskSeenCount.keySet()){
                            int currentCount=taskSeenCount.get(key);
                            if (currentCount>1){
                                int firstOccurrenceOfDuplicated = codeCS.indexOf(key);
                                codeCS.set(firstOccurrenceOfDuplicated,unassigned);
                                currentCount--;
                                taskSeenCount.put(key,currentCount);
                                break;
                            }
                        }
                    }
                    return new State(codeCS);
                }
            }
        }
        return crossedSolution;
    }

    enum FeasibilityTreatmentType {
        REJECT, REPAIR, PENALIZATION
    }
}
