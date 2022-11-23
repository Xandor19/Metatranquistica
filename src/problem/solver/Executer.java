package problem.solver;

import evolutionary_algorithms.complement.CrossoverType;
import evolutionary_algorithms.complement.MutationType;
import evolutionary_algorithms.complement.ReplaceType;
import evolutionary_algorithms.complement.SelectionType;
import local_search.complement.StopExecute;
import local_search.complement.UpdateParameter;
import metaheurictics.strategy.Strategy;
import metaheuristics.generators.GeneratorType;
import metaheuristics.generators.GeneticAlgorithm;
import metaheuristics.generators.HillClimbingRestart;
import problem.Definition;
import problem.codification.RoutingCodification;
import problem.definition.*;
import problem.extension.TypeSolutionMethod;
import problem.objectiveFunction.RoutingObjectiveFunction;
import problem.operator.InversionOperator;
import problem.operator.RoutingOperator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class Executer {

    /**
     * Cantidad de iteraciones del experimento
     */
    private int maxIterations;

    /**
     * Cantidad de soluciones vecinas en una corrida
     */
    private int neighbourhoodSize;

    /**
     * Constructor con la configuracion del experimento
     */
    public Executer(int maxIterations, int neighbourhoodSize) {
        this.maxIterations = maxIterations;
        this.neighbourhoodSize = neighbourhoodSize;
    }

    public static void main(String[] arg) throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        //Inicializacion de una instancia aleatoria del problema
        Definition.getDefinition().randomInstanceGeneration(1000, 500, 30);

        //Preparacion de la ejecucion
        Executer ex = new Executer(1000, 10);
        //Realizacion de los experimentos con la heuristica seleccionada
        ex.runExperiments("RS", "IV", 10, "");
    }

    /**
     * Establece la configuracion de este problema
     */
    private Problem configureProblem(String operatorType) {
        //Problema de optimizacion a resolver
        Problem problem = new Problem();
        //Funcion objetivo del problema
        ObjetiveFunction objetiveFunction = new RoutingObjectiveFunction();
        //Se construye una lista de funciones objetivo para representar los objetivos a optimizar
        ArrayList<ObjetiveFunction> objectives = new ArrayList<>();
        //Se agrega a la lista la unica funcion objetivo del problema
        objectives.add(objetiveFunction);
        //Se define la funcion objetivo a optimizar para el problema
        problem.setFunction(objectives);
        //Estrategia para la construccion de soluciones y la estructura de vecindades
        Operator operator;

        switch (operatorType) {
            case "IV": operator = new InversionOperator();
            break;

            case "RS": operator = new RoutingOperator();
            break;

            default: throw new IllegalArgumentException();
        }

        problem.setOperator(operator);
        //Validacion de soluciones y generacion de valores aleatorios para las variables
        Codification codification = new RoutingCodification();
        problem.setCodification(codification);
        //Se define si el problema es de Maximizacion o Minimizacion, en este caso se requiere minimizar el tiemmpo
        problem.setTypeProblem(Problem.ProblemType.Minimizar);
        //Se especifica si el problema tiene una sola funcion objetivo o si se van a optimizar varios objetivos
        problem.setTypeSolutionMethod(TypeSolutionMethod.MonoObjetivo);
        return problem;
    }

    /**
     * Ejecuta la heuristica seleccionada y salva los resultados
     */
    public void runExperiments(String algorithm, String operator, int executions, String resultsPath) throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        if (algorithm == null || algorithm.isEmpty()) {
            throw new RuntimeException("Especifique un algoritmo Ej: HC,TS,SA,EE,GA");
        }
        if (resultsPath == null || resultsPath.isEmpty()) {
            resultsPath = "results" + File.separator + algorithm;
            File file = new File(resultsPath);
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        List<ExecutionInformation> executionInformations = runAlgorithmExecutions(algorithm, operator, executions);
        //Save best solution evaluation to file
        saveEvaluationByExecution(resultsPath, executionInformations);
        //Save reference solution by iteration
        saveEvaluationByIteration(resultsPath, executionInformations);
        saveCandidateSolutionEvaluationByIteration(resultsPath, executionInformations);
    }

    /**
     * Prepara la ejecucion de la heuristica seleccionada
     */
    private List<ExecutionInformation> runAlgorithmExecutions(String algorithm, String operator, int executions) throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        List<ExecutionInformation> executionsInformation = new ArrayList<>();
        ExecutionInformation ei = new ExecutionInformation();
        switch (algorithm) {
            //Ejecucion de un Algoritmo Genetico
            case "GA": {
                for (int i = 0; i < executions; i++) {
                    executeGeneticAlgorithm_SteadyStateReplace_RouletteSelection(operator);
                    ei.bestSolutionFound = Strategy.getStrategy().getBestState();
                    ei.referenceSolutionByIteration = Strategy.getStrategy().listBest;
                    ei.candidateSolutionByIteration = Strategy.getStrategy().listStates;
                    ei.execution = i;
                    executionsInformation.add(ei);
                    Strategy.destroyExecute();
                }
            }
            break;
            //Ejecucion del Escalador de Colinas con reinicio
            case "HCR": {
                for (int i = 0; i < executions; i++) {
                    executeHillClimbingRestart(operator);
                    ei.bestSolutionFound = Strategy.getStrategy().getBestState();
                    ei.referenceSolutionByIteration = Strategy.getStrategy().listBest;
                    ei.candidateSolutionByIteration = Strategy.getStrategy().listStates;
                    ei.execution = i;
                    executionsInformation.add(ei);
                    Strategy.destroyExecute();
                }
            }break;
            //Por defecto se utiliza la Busqueda Aleatoria
            default:{
                for (int i = 0; i < executions; i++) {
                    executeRandomSearch(operator);
                    ei.bestSolutionFound = Strategy.getStrategy().getBestState();
                    ei.referenceSolutionByIteration = Strategy.getStrategy().listBest;
                    ei.candidateSolutionByIteration = Strategy.getStrategy().listStates;
                    ei.execution = i;
                    executionsInformation.add(ei);
                    Strategy.destroyExecute();
                }
            }
        }
        return executionsInformation;
    }

    /**
     * Configuracion para un Escalador de Colinas con reinicio
     */
    public void executeHillClimbingRestart(String operator) throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        Strategy.getStrategy().setStopexecute(new StopExecute());
        Strategy.getStrategy().setUpdateparameter(new UpdateParameter());
        //Se inicializa el problema de optimizacion
        Problem problem = configureProblem(operator);
        //Se define el problema de optimizacion a resolver
        Strategy.getStrategy().setProblem(problem);
        //Opcion para validar las soluciones
        Strategy.getStrategy().validate = false;
        Strategy.getStrategy().saveListBestStates = true;
        Strategy.getStrategy().saveListStates = true;
        //Se configura la metaheuristica para que reinicie la busqueda a las 100 iteraciones
        HillClimbingRestart.count = 250;
        //Se aplica la metaheuristica por el numero de iteraciones especificado, con un tamanno de vecindad y con la metaheuristica especificada por GeneratorType
        Strategy.getStrategy().executeStrategy(maxIterations, neighbourhoodSize, GeneratorType.HillClimbingRestart);
    }

    /**
     * Configuracion para un Algoritmo Genetico con seleccion por ruleta
     */
    public void executeGeneticAlgorithm_SteadyStateReplace_RouletteSelection(String operator) throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        Strategy.getStrategy().setStopexecute(new StopExecute());
        Strategy.getStrategy().setUpdateparameter(new UpdateParameter());
        //Se inicializa el problema de optimizacion
        Problem problem = configureProblem(operator);
        //Se define el problema de optimizacion a resolver
        Strategy.getStrategy().setProblem(problem);
        Strategy.getStrategy().initialize();
        //Opcion para validar las soluciones
        Strategy.getStrategy().validate = false;
        Strategy.getStrategy().saveListBestStates = true;
        Strategy.getStrategy().saveListStates = true;
        //Se define el tamanno de la poblacion de soluciones
        GeneticAlgorithm.countRef = 20;
        //Se especifica a la metaheuristica que utilice la estrategia de mutacion definida en la interfaz Operator
        GeneticAlgorithm.mutationType = MutationType.GenericMutation;
        //Se define una probabilidad de mutacion de 0.8
        GeneticAlgorithm.PM = 0.25;
        //Se define como estrategia de reemplazo Estado Estable (va a tomar soluciones de la poblacion de referencia y la nueva generada en cada iteracion)
        GeneticAlgorithm.replaceType = ReplaceType.SteadyStateReplace;
        //Se define como estrategia de seleccion Ruleta de probabilidades a partir del fitness de cada individuo
        GeneticAlgorithm.selectionType = SelectionType.TournamentSelection;
        //Se especifica a la metaheuristica que utilice como operador de cruzamiento el definido en la interfaz Operator
        GeneticAlgorithm.crossoverType = CrossoverType.GenericCrossover;
        //Se define que en cada iteracion hay una probabilidad del 50% de realizar un cruzamiento
        GeneticAlgorithm.PC = 0.8;
        //Se aplica la metaheuristica por el numero de iteraciones especificado, con un tamanno de vecindad y con la metaheuristica especificada por GeneratorType
        Strategy.getStrategy().executeStrategy(maxIterations, neighbourhoodSize, GeneratorType.GeneticAlgorithm);
    }

    /**
     * Configuracion para una Busqueda Aleatoria
     */
    public void executeRandomSearch(String operator) throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        Strategy.getStrategy().setStopexecute(new StopExecute());
        Strategy.getStrategy().setUpdateparameter(new UpdateParameter());
        //Se inicializa el problema de optimizacion
        Problem problem = configureProblem(operator);
        //Se define el problema de optimizacion a resolver
        Strategy.getStrategy().setProblem(problem);
        //Opcion para validar las soluciones
        Strategy.getStrategy().validate = false;
        Strategy.getStrategy().saveListBestStates = true;
        Strategy.getStrategy().saveListStates = true;
        //Se aplica la metaheuristica por el numero de iteraciones especificado, con un tamanno de vecindad y con la metaheuristica especificada por GeneratorType
        Strategy.getStrategy().executeStrategy(maxIterations, neighbourhoodSize, GeneratorType.RandomSearch);
    }

    /**
     * Salva de los resultados de una iteracion
     */
    private void saveEvaluationByIteration(String resultsPath, List<ExecutionInformation> executionInformations) {
        String outputPath = resultsPath + File.separator + "Detailed_execution_info";
        for (ExecutionInformation ei : executionInformations) {
            int execution = ei.execution + 1;
            StringBuilder data = new StringBuilder("Iteration,Evaluation\n");
            for (int i = 0; i < ei.referenceSolutionByIteration.size(); i++) {
                data.append(i).append(",").append(ei.referenceSolutionByIteration.get(i).getEvaluation().get(0)).append('\n');
            }
            File outputFile = new File(outputPath + File.separator + "Reference_solution_x_iterations_exec" + execution + ".csv");
            if (!outputFile.getParentFile().exists()) {
                outputFile.getParentFile().mkdirs();
            }
            try (PrintWriter pw = new PrintWriter(outputFile)) {
                pw.print(data);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("Evaluations by iterations saved at: " + outputPath);
    }

    /**
     * Salva de la solucion candidata de una iteracion
     */
    private void saveCandidateSolutionEvaluationByIteration(String resultsPath, List<ExecutionInformation> executionInformations) {
        String outputPath = resultsPath + File.separator + "Detailed_execution_info";
        for (ExecutionInformation ei : executionInformations) {
            int execution = ei.execution + 1;
            StringBuilder data = new StringBuilder("Iteration,Evaluation\n");
            for (int i = 0; i < ei.candidateSolutionByIteration.size(); i++) {
                data.append(i).append(",").append(ei.candidateSolutionByIteration.get(i).getEvaluation().get(0)).append('\n');
            }
            File outputFile = new File(outputPath + File.separator + "Candidate_solution_x_iterations_exec" + execution + ".csv");
            if (!outputFile.getParentFile().exists()) {
                outputFile.getParentFile().mkdirs();
            }
            try (PrintWriter pw = new PrintWriter(outputFile)) {
                pw.print(data);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("Evaluations by iterations saved at: " + outputPath);
    }

    /**
     * Salva de la evaluacion final de una ejecucion
     */
    private void saveEvaluationByExecution(String resultsPath, List<ExecutionInformation> executionInformations) {
        StringBuilder data = new StringBuilder("Ejecucion,Evaluacion\n");
        double average = 0;
        List<Double> sortedEvals = executionInformations.stream().map(ExecutionInformation::bestSolutionEvaluation).sorted().collect(Collectors.toList());
        double median = sortedEvals.get(sortedEvals.size() / 2);
        for (ExecutionInformation ei : executionInformations) {
            data.append(ei.execution).append(",").append(ei.bestSolutionEvaluation()).append('\n');
            average += ei.bestSolutionEvaluation();
        }
        data.append("Promedio:,").append(average / executionInformations.size()).append('\n');
        data.append("Mediana:,").append(median).append('\n');
        File evaluationData = new File(resultsPath + File.separator + "Evaluation by execution.csv");
        try (PrintWriter printWriter = new PrintWriter(evaluationData)) {
            printWriter.print(data);
            System.out.println("Evaluations saved at:" + evaluationData.getAbsolutePath());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Clase interna para representar las soluciones existentes en un momento dado
     */
    static class ExecutionInformation {
        List<State> referenceSolutionByIteration;
        List<State> candidateSolutionByIteration;
        State bestSolutionFound;
        int execution;

        public double bestSolutionEvaluation() {
            return bestSolutionFound.getEvaluation().get(0);
        }
    }
}