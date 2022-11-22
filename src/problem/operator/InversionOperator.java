package problem.operator;

import problem.Definition;
import problem.definition.State;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class InversionOperator extends RoutingOperator {
    /**
     * Define la estructura de vecindades del problema.
     * Genera una lista de soluciones vecinas a la solución recibida con el tamanno recibido.
     * La generacion se realiza mediante la heuristica de inversion de subcadena
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

            //Obtener los extremos del intercambio
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

            //Se obtiene el tramo inicial del recorrido hasta el primer punto de intercambio
            neighbourCode = new ArrayList<>(state.getCode().subList(0, Math.min(dest0, dest1)));

            //Se obtienen los destinos entre los puntos de intercambio
            List<Object> replaced = state.getCode().subList(Math.min(dest0, dest1), Math.max(dest0, dest1));
            //Se invierte el orden de los destinos intermedios
            Collections.reverse(replaced);
            //Se agrega el nuevo orden al recorrido
            neighbourCode.addAll(replaced);
            //Se obtiene el tramo final del recorrido
            neighbourCode.addAll(state.getCode().subList(Math.max(dest0, dest1), state.getCode().size()));

            //Asignar los nuevos valores de las variables de decision a la solucion vecina
            neighbour.setCode(neighbourCode);

            //Agregar la nueva solucion a la lista de soluciones vecinas
            neighbourhood.add(neighbour);
        }
        //Devolver la vecindad generada
        return neighbourhood;
    }
}
