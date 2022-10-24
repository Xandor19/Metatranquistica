package problem.objective.function;

import metaheurictics.strategy.Strategy;
import problem.Definition;
import problem.definition.ObjetiveFunction;
import problem.definition.State;

import java.util.HashSet;
import java.util.Set;

public class RoutingObjectiveFunction extends ObjetiveFunction {
    /**
     * Define el calculo de la funcion objetivo para una solucion del problema. En el caso de Asignacion de tareas define el costo total de la asignacion de las tareas a los trabajadores.
     *
     * @param state Solucion a evaluar.
     * @return Costo total de la asignacion representada en la solucion.
     */
    @Override
    public Double Evaluation(State state) {
        //Inicializacion del costo
        double totalAssignmentCost = 0;
        //Cantidad de destinos
        int amountDestinations = Definition.getDefinition().getAmountDestinations() - 1;

        //Obtiene el costo entre cada par de origen/destino
        for (int w = 0; w < amountDestinations - 1; w++) {
            int from = (int) state.getCode().get(w);
            int to = (int) state.getCode().get(w + 1);
            totalAssignmentCost += Definition.getDefinition().getCostBetween(from, to);
        }
        //Agrega el costo desde el ultimo destino al origen
        totalAssignmentCost += Definition.getDefinition().getCostBetween(amountDestinations, 0);

        if (!Strategy.getStrategy().getProblem().getCodification().validState(state)) {
            //TODO ¿vamos a poner penalizacion?
        }
        return totalAssignmentCost;
    }

    /**
     * Se define el costo de la penalizacion como el costo promedio entre cada par trabajador-tarea. Otra estrategia puede ser seleccionar el costo maximo o el costo minimo, o simplemente un valor constante.
     *
     * @return Costo de penalizacion
     */
    private double penaltyCost() {
        //TODO implementar penalizacion
        return 0;
    }
}
