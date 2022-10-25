package problem.objective.function;

import metaheurictics.strategy.Strategy;
import problem.Definition;
import problem.definition.ObjetiveFunction;
import problem.definition.State;

public class RoutingObjectiveFunction extends ObjetiveFunction {
    /**
     * Evalua una solucion con la funcion objetivo
     * En el caso del problema, la funcion objetivo busca minimizar el tiempo
     * (distancia/velocidad) de una ruta
     *
     * @param state Solucion a evaluar.
     * @return Costo total de la ruta representada en la solucion.
     */
    @Override
    public Double Evaluation(State state) {
        //Inicializacion del costo
        double totalPathCost = 0;
        //Cantidad de destinos
        int amountDestinations = Definition.getDefinition().getAmountDestinations() - 1;

        //Obtiene el costo entre cada par de origen/destino
        for (int w = 0; w < amountDestinations - 1; w++) {
            int from = (int) state.getCode().get(w);
            int to = (int) state.getCode().get(w + 1);
            totalPathCost += Definition.getDefinition().getCostBetween(from, to);
        }
        //Agrega el costo desde el ultimo destino al origen
        totalPathCost += Definition.getDefinition().getCostBetween(amountDestinations, 0);

        return totalPathCost / Definition.getDefinition().getRouteSpeed();
    }
}
