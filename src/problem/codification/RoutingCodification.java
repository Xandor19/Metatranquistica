package problem.codification;

import problem.Definition;
import problem.definition.Codification;
import problem.definition.State;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class RoutingCodification extends Codification {
    /**
     * Define si una solucion del problema cumple con las restricciones o no.
     * En este caso, una solucion valida debe cumplir que:
     * -Todos los destinos sean visitados
     * -Todos los destinos sean visitados solo una vez
     * -Los destinos con productos priorizados sean visitados antes que el resto
     * @param state Solucion a validar
     * @return true si la solucion es valida, false en caso contrario
     */
    @Override
    public boolean validState(State state) {
        List<Object> code= state.getCode();

        //Comprueba que el camino tenga la cantidad de destinos establecida
        if (code.size() == Definition.getDefinition().getAmountDestinations()) {
            //Verifica que el camino comience por el punto de partida
            if ( (int) code.get(0) == 0) {
                Set<Object> seenAllocations = new HashSet<>(code);

                //Si el set tiene la misma cantidad de elementos que el estado, entonces no hay repetidos y se visitaron todos
                if (code.size() == seenAllocations.size()) {
                    //Obtiene los primeros destinos visitados que deben coincidir con los destinos priorizados
                    List<Object> mustBePrior = code.subList(1, Definition.getDefinition().getAmountPrioritized() + 1);

                    //Comprueba que los destinos obtenidos sean priorizados
                    for (Object o : mustBePrior) {
                        if (!Definition.getDefinition().isPrioritized((int) o)) {
                            return false;
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Genera un posible valor aleatorio para la variable especificada.
     * @param variableIndex Indice de la variable
     * @return Valor aleatorio de la variable
     */
    @Override
    public Object getVariableAleatoryValue(int variableIndex) {
        //Generador de numeros aleatorios a utilizar
        Random randomGenerator= Definition.getDefinition().getRandomGenerator();

        return randomGenerator.nextInt(Definition.getDefinition().getAmountDestinations());
    }

    /**
     * Genera un valor aleatorio que coincide con el indice de una de las variables de decision
     * @return Indice de la variable de decision
     */
    @Override
    public int getAleatoryKey() {
        //Generador de numeros aleatorios a utilizar
        Random randomGenerator= Definition.getDefinition().getRandomGenerator();

        return randomGenerator.nextInt(Definition.getDefinition().getAmountDestinations());
    }

    /**
     * Devuelve la cantidad de variables de decision para la instancia del problema
     * @return cantidad de variables de decision
     */
    @Override
    public int getVariableCount() {
        //complete el codigo aqui
        return Definition.getDefinition().getAmountDestinations();
    }
}
