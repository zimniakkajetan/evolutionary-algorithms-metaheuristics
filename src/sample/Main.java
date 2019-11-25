package sample;

import javafx.application.Application;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class Main extends Application {
    /**
     * Number of groups (clusters)
     */
    private final static int GROUPS = 20;
    /**
     * Number of iterations in test stage
     */
    private final static int TESTS_NUMBER = 10;
    /**
     * Should we show extra logs and statistics?
     */
    private final static boolean SHOW_STATISTICS = true;
    /**
     * Should we use following algorithms?
     */
    private final static boolean EXECUTE_GREEDY_NAIVE = false;
    private final static boolean EXECUTE_GREEDY_RANDOM = false;
    private final static boolean EXECUTE_STEEPEST_NAIVE = false;
    private final static boolean EXECUTE_STEEPEST_RANDOM = false;
    private final static boolean EXECUTE_STEEPEST_CANDIDATE = false;
    private final static boolean EXECUTE_STEEPEST_CANDIDATE_CACHE = false;
    private final static boolean EXECUTE_STEEPEST_CACHE = false;
    private final static boolean EXECUTE_MSLS = true;
    private final static boolean EXECUTE_ILS_SMALL_PERTURBATION = false;
    private final static boolean EXECUTE_ILS_BIG_PERTURBATION = false;
    private final static boolean CALCULATE_STAT_QUALITY_SIMILARITY = false;
    private final static boolean EXECUTE_EVOLUTIONARY = true;

    /**
     * How many candidates we chose in steepest naive candidates algorithm
     */
    private final static int CANDIDATES_NUMBER = 10;
    private final static int MSLS_LS_ITERATIONS = 100;
    private final static int EVOLUTIONARY_CACHE_SIZE = 15;
    /**
     * Mode - Greedy when 0 else Steepest
     */
    private final static int MSLS_MODE = 0;

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * @param startIndexesList Start assignment (indexes)
     * @param coordinates      All points list
     * @return Random assignment to groups
     */
    public static HashMap<Integer, HashSet<Integer>> randomInitGroups(ArrayList<Integer> startIndexesList, ArrayList<PointCoordinates> coordinates) {
        HashMap<Integer, HashSet<Integer>> elementsWithAssignmentToGroups = new HashMap<>();
        Random random = new Random();

        // Initialize groups
        for (int i = 0; i < GROUPS; i++) {
            HashSet<Integer> set = new HashSet<>();
            set.add(startIndexesList.get(i));
            elementsWithAssignmentToGroups.put(i, set);
        }

        // Assign each point to group
        for (PointCoordinates point : coordinates) {
            // Add point only when not in start
            if (!startIndexesList.contains(point.getID())) {
                elementsWithAssignmentToGroups.get(random.nextInt(GROUPS)).add(point.getID());
            }
        }

        return elementsWithAssignmentToGroups;
    }

    @Override
    public void start(Stage primaryStage) {
        Reader reader = new Reader();
        ArrayList<PointCoordinates> coordinates = reader.readInstance("instances/objects20_06.data");

        EuclideanDistance euclideanDistance = new EuclideanDistance();
        double[][] distanceMatrix = euclideanDistance.calculateDistanceMatrix(coordinates);

        HashSet<ArrayList<PointsPath>> bestNaiveGreedyGroupsMST = new HashSet<>();
        HashSet<ArrayList<PointsPath>> bestNaiveGreedyGroupsConnections = new HashSet<>();
        HashSet<ArrayList<PointsPath>> bestNaiveSteepestGroupsMST = new HashSet<>();
        HashSet<ArrayList<PointsPath>> bestNaiveSteepestGroupsConnections = new HashSet<>();
        HashSet<ArrayList<PointsPath>> bestRandomGreedyGroupsMST = new HashSet<>();
        HashSet<ArrayList<PointsPath>> bestRandomGreedyGroupsConnections = new HashSet<>();
        HashSet<ArrayList<PointsPath>> bestRandomSteepestGroupsMST = new HashSet<>();
        HashSet<ArrayList<PointsPath>> bestRandomSteepestGroupsConnections = new HashSet<>();
        HashSet<ArrayList<PointsPath>> bestNaiveSteepestCandidateGroupsMST = new HashSet<>();
        HashSet<ArrayList<PointsPath>> bestNaiveSteepestCacheGroupsMST = new HashSet<>();
        HashSet<ArrayList<PointsPath>> bestNaiveSteepestCandidateCacheGroupsMST = new HashSet<>();
        HashSet<ArrayList<PointsPath>> bestNaiveSteepestCandidateGroupsConnections = new HashSet<>();
        HashSet<ArrayList<PointsPath>> bestNaiveSteepestCacheGroupsConnections = new HashSet<>();
        HashSet<ArrayList<PointsPath>> bestNaiveSteepestCandidateCacheGroupsConnections = new HashSet<>();
        HashSet<ArrayList<PointsPath>> bestMSLSGroupsMST = new HashSet<>();
        HashSet<ArrayList<PointsPath>> bestMSLSGroupsConnections = new HashSet<>();
        HashSet<ArrayList<PointsPath>> bestILSSmallPerturbationGroupsMST = new HashSet<>();
        HashSet<ArrayList<PointsPath>> bestILSSmallPerturbationGroupsConnections = new HashSet<>();
        HashSet<ArrayList<PointsPath>> bestILSBigPerturbationGroupsMST = new HashSet<>();
        HashSet<ArrayList<PointsPath>> bestILSBigPerturbationGroupsConnections = new HashSet<>();
        HashSet<ArrayList<PointsPath>> bestILSBigHeuristicPerturbationGroupsMST = new HashSet<>();
        HashSet<ArrayList<PointsPath>> bestILSBigHeuristicPerturbationGroupsConnections = new HashSet<>();
        HashSet<ArrayList<PointsPath>> bestEvolutionaryGroupsMST = new HashSet<>();
        HashSet<ArrayList<PointsPath>> bestEvolutionaryGroupsConnections = new HashSet<>();

        List<int[]> randomGreedySolutionsList = new ArrayList<>();
        int[] bestGreedySolution = new int[coordinates.size()];

        double[] naiveGreedyResults = new double[TESTS_NUMBER], naiveSteepestResults = new double[TESTS_NUMBER],
                randomGreedyResults = new double[TESTS_NUMBER], randomSteepestResults = new double[TESTS_NUMBER],
                naiveSteepestCandidateResults = new double[TESTS_NUMBER], naiveSteepestCacheResults = new double[TESTS_NUMBER],
                naiveSteepestCandidateCacheResults = new double[TESTS_NUMBER],
                naiveGreedyTimes = new double[TESTS_NUMBER], naiveSteepestTimes = new double[TESTS_NUMBER],
                randomGreedyTimes = new double[TESTS_NUMBER], randomSteepestTimes = new double[TESTS_NUMBER],
                naiveSteepestCandidateTimes = new double[TESTS_NUMBER], naiveSteepestCacheTimes = new double[TESTS_NUMBER],
                naiveSteepestCandidateCacheTimes = new double[TESTS_NUMBER],
                ILSSmallPerturbationTimes = new double[TESTS_NUMBER], ILSSmallPerturbationResults = new double[TESTS_NUMBER],
                ILSBigPerturbationTimes = new double[TESTS_NUMBER], ILSBigPerturbationResults = new double[TESTS_NUMBER],
                ILSBigHeuristicPerturbationTimes = new double[TESTS_NUMBER], ILSBigHeuristicPerturbationResults = new double[TESTS_NUMBER],
                MSLSTimes = new double[TESTS_NUMBER], MSLSResults = new double[TESTS_NUMBER],
                EvolutionaryTimes = new double[TESTS_NUMBER], EvolutionaryResults = new double[TESTS_NUMBER];

        double bestNaiveGreedyResult = Double.MAX_VALUE, bestRandomGreedyResult = Double.MAX_VALUE,
                bestNaiveSteepestResult = Double.MAX_VALUE, bestRandomSteepestResult = Double.MAX_VALUE,
                bestNaiveSteepestCandidateResult = Double.MAX_VALUE, bestNaiveSteepestCacheResult = Double.MAX_VALUE,
                bestNaiveSteepestCandidateCacheResult = Double.MAX_VALUE, bestMSLSResult = Double.MAX_VALUE,
                bestILSSmallPerturbationResult = Double.MAX_VALUE, bestILSBigPerturbationResult = Double.MAX_VALUE,
                bestILSBigHeuristicPerturbationResult = Double.MAX_VALUE, bestEvolutionaryResult = Double.MAX_VALUE;

        // Using for statistics
        long startTime;
        int totalElementsLength = coordinates.size();

        // Iterations
        for (int iteration = 0; iteration < TESTS_NUMBER; iteration++) {
            System.out.println("Iteration " + (iteration + 1));
            /*
             * INITIALIZATION STEP IN ITERATION
             */
            Random random = new Random();

            // Generate randomized indexes of start points
            HashSet<Integer> startIndexesSet = new HashSet<>();
            while (startIndexesSet.size() < GROUPS) {
                startIndexesSet.add(random.nextInt(totalElementsLength));
            }

            // Cast set to list
            ArrayList<Integer> startIndexesList = new ArrayList<>(startIndexesSet);

            // Naive and random instances
            HashMap<Integer, HashSet<Integer>> naiveInstances = naiveAlgorithm(distanceMatrix, startIndexesList, coordinates);
            HashMap<Integer, HashSet<Integer>> randomInstances = randomInitGroups(startIndexesList, coordinates);

            /*
             * GREEDY NAIVE
             */
            if (EXECUTE_GREEDY_NAIVE) {
                startTime = System.nanoTime();
                GreedyLocalSolver naiveGreedyLocalSolver = new GreedyLocalSolver(naiveInstances);
                naiveGreedyLocalSolver.run(distanceMatrix);
                naiveGreedyResults[iteration] = naiveGreedyLocalSolver.getPenalties();
                if (naiveGreedyLocalSolver.getPenalties() < bestNaiveGreedyResult) {
                    bestNaiveGreedyResult = naiveGreedyLocalSolver.getPenalties();
                    bestNaiveGreedyGroupsMST = castLocalSearchToMSTGraph(naiveGreedyLocalSolver.getGroups(), distanceMatrix);
                    bestNaiveGreedyGroupsConnections = castLocalSearchToConnectionGraph(naiveGreedyLocalSolver.getGroups(), distanceMatrix);
                }
                naiveGreedyTimes[iteration] = System.nanoTime() - startTime;
            }
            /*
             * GREEDY RANDOM
             */
            if (EXECUTE_GREEDY_RANDOM) {
                startTime = System.nanoTime();
                GreedyLocalSolver randomGreedyLocalSolver = new GreedyLocalSolver(randomInstances);
                randomGreedyLocalSolver.run(distanceMatrix);

                // If enabled similarity - calculate stats
                int[] pointsGroups = new int[totalElementsLength + 1];
                if (CALCULATE_STAT_QUALITY_SIMILARITY) {
                    HashMap<Integer, HashSet<Integer>> groups = randomGreedyLocalSolver.getGroups();
                    for (Map.Entry<Integer, HashSet<Integer>> entry : groups.entrySet()) {
                        for (Integer id : entry.getValue()) {
                            pointsGroups[id] = entry.getKey();
                        }
                    }
                    randomGreedySolutionsList.add(pointsGroups);
                }

                randomGreedyResults[iteration] = randomGreedyLocalSolver.getPenalties();
                if (randomGreedyLocalSolver.getPenalties() < bestRandomGreedyResult) {
                    if (CALCULATE_STAT_QUALITY_SIMILARITY) {
                        // Using in quality-similarity statistic
                        bestGreedySolution = pointsGroups;
                    }

                    bestRandomGreedyResult = randomGreedyLocalSolver.getPenalties();
                    bestRandomGreedyGroupsMST = castLocalSearchToMSTGraph(randomGreedyLocalSolver.getGroups(), distanceMatrix);
                    bestRandomGreedyGroupsConnections = castLocalSearchToConnectionGraph(randomGreedyLocalSolver.getGroups(), distanceMatrix);
                }
                randomGreedyTimes[iteration] = System.nanoTime() - startTime;
            }
            /*
             * STEEPEST NAIVE
             */
            if (EXECUTE_STEEPEST_NAIVE) {
                startTime = System.nanoTime();
                SteepestLocalSolver naiveSteepestLocalSolver = new SteepestLocalSolver(naiveInstances, false, CANDIDATES_NUMBER, false);
                naiveSteepestLocalSolver.run(distanceMatrix);
                naiveSteepestResults[iteration] = naiveSteepestLocalSolver.getPenalties();
                if (naiveSteepestLocalSolver.getPenalties() < bestNaiveSteepestResult) {
                    bestNaiveSteepestResult = naiveSteepestLocalSolver.getPenalties();
                    bestNaiveSteepestGroupsMST = castLocalSearchToMSTGraph(naiveSteepestLocalSolver.getGroups(), distanceMatrix);
                    bestNaiveSteepestGroupsConnections = castLocalSearchToConnectionGraph(naiveSteepestLocalSolver.getGroups(), distanceMatrix);
                }
                naiveSteepestTimes[iteration] = System.nanoTime() - startTime;
            }
            /*
             * STEEPEST RANDOM
             */
            if (EXECUTE_STEEPEST_RANDOM) {
                startTime = System.nanoTime();
                SteepestLocalSolver randomSteepestLocalSolver = new SteepestLocalSolver(randomInstances, false, CANDIDATES_NUMBER, false);
                randomSteepestLocalSolver.run(distanceMatrix);
                randomSteepestResults[iteration] = randomSteepestLocalSolver.getPenalties();
                if (randomSteepestLocalSolver.getPenalties() < bestRandomSteepestResult) {
                    bestRandomSteepestResult = randomSteepestLocalSolver.getPenalties();
                    bestRandomSteepestGroupsMST = castLocalSearchToMSTGraph(randomSteepestLocalSolver.getGroups(), distanceMatrix);
                    bestRandomSteepestGroupsConnections = castLocalSearchToConnectionGraph(randomSteepestLocalSolver.getGroups(), distanceMatrix);
                }
                randomSteepestTimes[iteration] = System.nanoTime() - startTime;
            }
            /*
             * STEEPEST CANDIDATES
             */
            if (EXECUTE_STEEPEST_CANDIDATE) {
                startTime = System.nanoTime();
                SteepestLocalSolver naiveSteepestLocalSolver = new SteepestLocalSolver(naiveInstances, true, CANDIDATES_NUMBER, false);
                naiveSteepestLocalSolver.run(distanceMatrix);
                naiveSteepestCandidateResults[iteration] = naiveSteepestLocalSolver.getPenalties();
                if (naiveSteepestLocalSolver.getPenalties() < bestNaiveSteepestCandidateResult) {
                    bestNaiveSteepestCandidateResult = naiveSteepestLocalSolver.getPenalties();
                    bestNaiveSteepestCandidateGroupsMST = castLocalSearchToMSTGraph(naiveSteepestLocalSolver.getGroups(), distanceMatrix);
                    bestNaiveSteepestCandidateGroupsConnections = castLocalSearchToConnectionGraph(naiveSteepestLocalSolver.getGroups(), distanceMatrix);
                }
                naiveSteepestCandidateTimes[iteration] = System.nanoTime() - startTime;
            }
            /*
             * STEEPEST CACHE
             */
            if (EXECUTE_STEEPEST_CACHE) {
                startTime = System.nanoTime();
                SteepestLocalSolver naiveSteepestLocalSolver = new SteepestLocalSolver(naiveInstances, false, CANDIDATES_NUMBER, true);
                naiveSteepestLocalSolver.run(distanceMatrix);
                naiveSteepestCacheResults[iteration] = naiveSteepestLocalSolver.getPenalties();
                if (naiveSteepestLocalSolver.getPenalties() < bestNaiveSteepestCacheResult) {
                    bestNaiveSteepestCacheResult = naiveSteepestLocalSolver.getPenalties();
                    bestNaiveSteepestCacheGroupsMST = castLocalSearchToMSTGraph(naiveSteepestLocalSolver.getGroups(), distanceMatrix);
                    bestNaiveSteepestCacheGroupsConnections = castLocalSearchToConnectionGraph(naiveSteepestLocalSolver.getGroups(), distanceMatrix);
                }
                naiveSteepestCacheTimes[iteration] = System.nanoTime() - startTime;
            }
            /*
             * STEEPEST CANDIDATES + CACHE
             */
            if (EXECUTE_STEEPEST_CANDIDATE_CACHE) {
                startTime = System.nanoTime();
                SteepestLocalSolver naiveSteepestLocalSolver = new SteepestLocalSolver(naiveInstances, true, CANDIDATES_NUMBER, true);
                naiveSteepestLocalSolver.run(distanceMatrix);
                naiveSteepestCandidateCacheResults[iteration] = naiveSteepestLocalSolver.getPenalties();
                if (naiveSteepestLocalSolver.getPenalties() < bestNaiveSteepestCandidateCacheResult) {
                    bestNaiveSteepestCandidateCacheResult = naiveSteepestLocalSolver.getPenalties();
                    bestNaiveSteepestCandidateCacheGroupsMST = castLocalSearchToMSTGraph(naiveSteepestLocalSolver.getGroups(), distanceMatrix);
                    bestNaiveSteepestCandidateCacheGroupsConnections = castLocalSearchToConnectionGraph(naiveSteepestLocalSolver.getGroups(), distanceMatrix);
                }
                naiveSteepestCandidateCacheTimes[iteration] = System.nanoTime() - startTime;
            }
            /*
             * MSLS
             */
            long search_time_limit = (long) (30.0 * 1_000_000_000.0);
            if (EXECUTE_MSLS) {
                search_time_limit = System.nanoTime();
                startTime = System.nanoTime();
                MSLS msls = new MSLS(MSLS_MODE, MSLS_LS_ITERATIONS, startIndexesList, distanceMatrix, coordinates);
                msls.run();

                MSLSResults[iteration] = msls.getMinPenalties();
                if (msls.getMinPenalties() < bestMSLSResult) {
                    bestMSLSResult = msls.getMinPenalties();
                    bestMSLSGroupsMST = (castLocalSearchToMSTGraph(msls.getGroups(), distanceMatrix));
                    bestMSLSGroupsConnections = (castLocalSearchToConnectionGraph(msls.getGroups(), distanceMatrix));
                }

                MSLSTimes[iteration] = System.nanoTime() - startTime;
                search_time_limit = System.nanoTime() - search_time_limit;
            }
            /*
             * Iterated Local Search with small perturbation
             */
            if (EXECUTE_ILS_SMALL_PERTURBATION) {
                startTime = System.nanoTime();
                IteratedLocalSearch iteratedLocalSolver = new IteratedLocalSearch(randomInstances, IteratedLocalSearch.MODE.SMALL);
                iteratedLocalSolver.run(distanceMatrix, search_time_limit);
                ILSSmallPerturbationResults[iteration] = iteratedLocalSolver.getBestPenalties();
                if (iteratedLocalSolver.getBestPenalties() < bestILSSmallPerturbationResult) {
                    bestILSSmallPerturbationResult = iteratedLocalSolver.getBestPenalties();
                    bestILSSmallPerturbationGroupsMST = castLocalSearchToMSTGraph(iteratedLocalSolver.getBestGroups(), distanceMatrix);
                    bestILSSmallPerturbationGroupsConnections = castLocalSearchToConnectionGraph(iteratedLocalSolver.getBestGroups(), distanceMatrix);
                }
                ILSSmallPerturbationTimes[iteration] = System.nanoTime() - startTime;
            }
            /*
             * Iterated Local Search with big random perturbation
             */
            if (EXECUTE_ILS_BIG_PERTURBATION) {
                startTime = System.nanoTime();
                IteratedLocalSearch iteratedLocalSolver = new IteratedLocalSearch(randomInstances, IteratedLocalSearch.MODE.BIG_RANDOM);
                iteratedLocalSolver.run(distanceMatrix, search_time_limit);
                ILSBigPerturbationResults[iteration] = iteratedLocalSolver.getBestPenalties();
                if (iteratedLocalSolver.getBestPenalties() < bestILSBigPerturbationResult) {
                    bestILSBigPerturbationResult = iteratedLocalSolver.getBestPenalties();
                    bestILSBigPerturbationGroupsMST = castLocalSearchToMSTGraph(iteratedLocalSolver.getBestGroups(), distanceMatrix);
                    bestILSBigPerturbationGroupsConnections = castLocalSearchToConnectionGraph(iteratedLocalSolver.getBestGroups(), distanceMatrix);
                }
                ILSBigPerturbationTimes[iteration] = System.nanoTime() - startTime;
            }
            /*
             * Iterated Local Search with big heuristic perturbation
             */
            if (EXECUTE_ILS_BIG_PERTURBATION) {
                startTime = System.nanoTime();
                IteratedLocalSearch iteratedLocalSolver = new IteratedLocalSearch(randomInstances, IteratedLocalSearch.MODE.BIG_HEURISTIC);
                iteratedLocalSolver.run(distanceMatrix, search_time_limit);
                ILSBigHeuristicPerturbationResults[iteration] = iteratedLocalSolver.getBestPenalties();
                if (iteratedLocalSolver.getBestPenalties() < bestILSBigHeuristicPerturbationResult) {
                    bestILSBigHeuristicPerturbationResult = iteratedLocalSolver.getBestPenalties();
                    bestILSBigHeuristicPerturbationGroupsMST = castLocalSearchToMSTGraph(iteratedLocalSolver.getBestGroups(), distanceMatrix);
                    bestILSBigHeuristicPerturbationGroupsConnections = castLocalSearchToConnectionGraph(iteratedLocalSolver.getBestGroups(), distanceMatrix);
                }
                ILSBigHeuristicPerturbationTimes[iteration] = System.nanoTime() - startTime;
            }
            /*
             * Evolutionary algorithm
             */
            if (EXECUTE_EVOLUTIONARY) {
                startTime = System.nanoTime();
                EvolutionaryAlgorithm solver = new EvolutionaryAlgorithm(coordinates, startIndexesList, EVOLUTIONARY_CACHE_SIZE, GROUPS);
                solver.run(distanceMatrix, search_time_limit);
                EvolutionaryResults[iteration] = solver.getBestPenalties();
                if (solver.getBestPenalties() < bestEvolutionaryResult) {
                    bestEvolutionaryResult = solver.getBestPenalties();
                    bestEvolutionaryGroupsMST = castLocalSearchToMSTGraph(solver.getBestGroups(), distanceMatrix);
                    bestEvolutionaryGroupsConnections = castLocalSearchToConnectionGraph(solver.getBestGroups(), distanceMatrix);
                }
                EvolutionaryTimes[iteration] = System.nanoTime() - startTime;
            }
        }

        if (CALCULATE_STAT_QUALITY_SIMILARITY) {
            int[] similarity = new int[TESTS_NUMBER];

            // Each solution compare with best
            Arrays.fill(similarity, 0);
            for (int i = 0; i < TESTS_NUMBER; i++) {
                // Compare each pair with that pair in best solution
                for (int p1 = 0; p1 < totalElementsLength; p1++) {
                    for (int p2 = p1 + 1; p2 < totalElementsLength; p2++) {
                        if (randomGreedySolutionsList.get(i)[p1] == randomGreedySolutionsList.get(i)[p2]
                                && bestGreedySolution[p1] == bestGreedySolution[p2]) {
                            similarity[i]++;
                        }
                    }
                }
            }
            saveCsv(similarity, "raport/sprawozdanie_5/stat_best.csv", randomGreedyResults);

            // Each solution compare with all
            Arrays.fill(similarity, 0);
            for (int i = 0; i < TESTS_NUMBER; i++) {
                for (int j = 0; j < TESTS_NUMBER; j++) {
                    if (i != j) {
                        // Compare each pair
                        for (int p1 = 0; p1 < totalElementsLength; p1++) {
                            for (int p2 = p1 + 1; p2 < totalElementsLength; p2++) {
                                if (randomGreedySolutionsList.get(i)[p1] == randomGreedySolutionsList.get(i)[p2]
                                        && randomGreedySolutionsList.get(j)[p1] == randomGreedySolutionsList.get(j)[p2]) {
                                    similarity[i]++;
                                }
                            }
                        }
                    }
                }
            }
            saveCsv(similarity, "raport/sprawozdanie_5/stat_all.csv", randomGreedyResults);
        }

        // Show groups on graph
        if (EXECUTE_GREEDY_NAIVE) {
            new Drawer().drawInputInstance(coordinates, bestNaiveGreedyGroupsMST, "Naive greedy", true, true);
            new Drawer().drawInputInstance(coordinates, bestNaiveGreedyGroupsMST, "Naive greedy", true, false);
            new Drawer().drawInputInstance(coordinates, bestNaiveGreedyGroupsConnections, "Naive greedy", false, true);
        }

        if (EXECUTE_GREEDY_RANDOM) {
            new Drawer().drawInputInstance(coordinates, bestRandomGreedyGroupsMST, "Random greedy", true, true);
            new Drawer().drawInputInstance(coordinates, bestRandomGreedyGroupsMST, "Random greedy", true, false);
            new Drawer().drawInputInstance(coordinates, bestRandomGreedyGroupsConnections, "Random greedy", false, true);
        }

        if (EXECUTE_STEEPEST_NAIVE) {
            new Drawer().drawInputInstance(coordinates, bestNaiveSteepestGroupsMST, "Naive steepest", true, true);
            new Drawer().drawInputInstance(coordinates, bestNaiveSteepestGroupsMST, "Naive steepest", true, false);
            new Drawer().drawInputInstance(coordinates, bestNaiveSteepestGroupsConnections, "Naive steepest", false, true);
        }

        if (EXECUTE_STEEPEST_RANDOM) {
            new Drawer().drawInputInstance(coordinates, bestRandomSteepestGroupsMST, "Random steepest", true, true);
            new Drawer().drawInputInstance(coordinates, bestRandomSteepestGroupsMST, "Random steepest", true, false);
            new Drawer().drawInputInstance(coordinates, bestRandomSteepestGroupsConnections, "Random steepest", false, true);
        }

        if (EXECUTE_STEEPEST_CANDIDATE) {
            new Drawer().drawInputInstance(coordinates, bestNaiveSteepestCandidateGroupsMST, "Naive steepest candidate", true, true);
            new Drawer().drawInputInstance(coordinates, bestNaiveSteepestCandidateGroupsMST, "Naive steepest candidate", true, false);
            new Drawer().drawInputInstance(coordinates, bestNaiveSteepestCandidateGroupsConnections, "Naive steepest candidate", false, true);
        }

        if (EXECUTE_STEEPEST_CACHE) {
            new Drawer().drawInputInstance(coordinates, bestNaiveSteepestCacheGroupsMST, "Naive steepest cache", true, true);
            new Drawer().drawInputInstance(coordinates, bestNaiveSteepestCacheGroupsMST, "Naive steepest cache", true, false);
            new Drawer().drawInputInstance(coordinates, bestNaiveSteepestCacheGroupsConnections, "Naive steepest cache", false, true);
        }

        if (EXECUTE_STEEPEST_CANDIDATE_CACHE) {
            new Drawer().drawInputInstance(coordinates, bestNaiveSteepestCandidateCacheGroupsMST, "Naive steepest candidate + cache", true, true);
            new Drawer().drawInputInstance(coordinates, bestNaiveSteepestCandidateCacheGroupsMST, "Naive steepest candidate + cache", true, false);
            new Drawer().drawInputInstance(coordinates, bestNaiveSteepestCandidateCacheGroupsConnections, "Naive steepest candidate + cache", false, true);
        }

        if (EXECUTE_MSLS) {
            new Drawer().drawInputInstance(coordinates, bestMSLSGroupsMST, "MSLS", true, true);
            new Drawer().drawInputInstance(coordinates, bestMSLSGroupsMST, "MSLS", true, false);
            new Drawer().drawInputInstance(coordinates, bestMSLSGroupsConnections, "MSLS", false, true);
        }

        if (EXECUTE_ILS_SMALL_PERTURBATION) {
            new Drawer().drawInputInstance(coordinates, bestILSSmallPerturbationGroupsMST, "ILS small perturbation", true, true);
            new Drawer().drawInputInstance(coordinates, bestILSSmallPerturbationGroupsMST, "ILS small perturbation", true, false);
            new Drawer().drawInputInstance(coordinates, bestILSSmallPerturbationGroupsConnections, "ILS small perturbation", false, true);
        }

        if (EXECUTE_ILS_BIG_PERTURBATION) {
            new Drawer().drawInputInstance(coordinates, bestILSBigPerturbationGroupsMST, "ILS big random perturbation", true, true);
            new Drawer().drawInputInstance(coordinates, bestILSBigPerturbationGroupsMST, "ILS big random perturbation", true, false);
            new Drawer().drawInputInstance(coordinates, bestILSBigPerturbationGroupsConnections, "ILS big random perturbation", false, true);
        }

        if (EXECUTE_ILS_BIG_PERTURBATION) {
            new Drawer().drawInputInstance(coordinates, bestILSBigHeuristicPerturbationGroupsMST, "ILS big heuristic perturbation", true, true);
            new Drawer().drawInputInstance(coordinates, bestILSBigHeuristicPerturbationGroupsMST, "ILS big heuristic perturbation", true, false);
            new Drawer().drawInputInstance(coordinates, bestILSBigHeuristicPerturbationGroupsConnections, "ILS big heuristic perturbation", false, true);
        }

        if (EXECUTE_EVOLUTIONARY) {
            new Drawer().drawInputInstance(coordinates, bestEvolutionaryGroupsMST, "Evolutionary", true, true);
            new Drawer().drawInputInstance(coordinates, bestEvolutionaryGroupsMST, "Evolutionary", true, false);
            new Drawer().drawInputInstance(coordinates, bestEvolutionaryGroupsConnections, "Evolutionary", false, true);
        }

        if (SHOW_STATISTICS) {
            if (EXECUTE_GREEDY_NAIVE)
                System.out.println("Min result for naive greedy = " + bestNaiveGreedyResult);
            if (EXECUTE_GREEDY_RANDOM)
                System.out.println("Min result for random greedy = " + bestRandomGreedyResult);
            if (EXECUTE_STEEPEST_NAIVE)
                System.out.println("Min result for naive steepest = " + bestNaiveSteepestResult);
            if (EXECUTE_STEEPEST_RANDOM)
                System.out.println("Min result for random steepest = " + bestRandomSteepestResult);
            if (EXECUTE_STEEPEST_CANDIDATE)
                System.out.println("Min result for naive steepest candidate = " + bestNaiveSteepestCandidateResult);
            if (EXECUTE_STEEPEST_CACHE)
                System.out.println("Min result for naive steepest cache = " + bestNaiveSteepestCacheResult);
            if (EXECUTE_STEEPEST_CANDIDATE_CACHE)
                System.out.println("Min result for naive steepest candidate + cache = " + bestNaiveSteepestCandidateCacheResult);
            if (EXECUTE_MSLS)
                System.out.println("Min result for MSLS = " + bestMSLSResult);
            if (EXECUTE_ILS_SMALL_PERTURBATION)
                System.out.println("Min result for ILS small perturbation = " + bestILSSmallPerturbationResult);
            if (EXECUTE_ILS_BIG_PERTURBATION)
                System.out.println("Min result for ILS big random perturbation = " + bestILSBigPerturbationResult);
            if (EXECUTE_ILS_BIG_PERTURBATION)
                System.out.println("Min result for ILS big heuristic perturbation = " + bestILSBigHeuristicPerturbationResult);
            if (EXECUTE_EVOLUTIONARY)
                System.out.println("Min result for Evolutionary = " + bestEvolutionaryResult);

            if (EXECUTE_GREEDY_NAIVE)
                System.out.println("Mean result for naive greedy = " + Arrays.stream(naiveGreedyResults).average().getAsDouble());
            if (EXECUTE_GREEDY_RANDOM)
                System.out.println("Mean result for random greedy = " + Arrays.stream(randomGreedyResults).average().getAsDouble());
            if (EXECUTE_STEEPEST_NAIVE)
                System.out.println("Mean result for naive steepest = " + Arrays.stream(naiveSteepestResults).average().getAsDouble());
            if (EXECUTE_STEEPEST_RANDOM)
                System.out.println("Mean result for random steepest = " + Arrays.stream(randomSteepestResults).average().getAsDouble());
            if (EXECUTE_STEEPEST_CANDIDATE)
                System.out.println("Mean result for naive steepest candidate = " + Arrays.stream(naiveSteepestCandidateResults).average().getAsDouble());
            if (EXECUTE_STEEPEST_CACHE)
                System.out.println("Mean result for naive steepest cache = " + Arrays.stream(naiveSteepestCacheResults).average().getAsDouble());
            if (EXECUTE_STEEPEST_CANDIDATE_CACHE)
                System.out.println("Mean result for naive steepest candidate + cache = " + Arrays.stream(naiveSteepestCandidateCacheResults).average().getAsDouble());
            if (EXECUTE_MSLS)
                System.out.println("Mean result for MSLS = " + Arrays.stream(MSLSResults).average().getAsDouble());
            if (EXECUTE_ILS_SMALL_PERTURBATION)
                System.out.println("Mean result for ILS small perturbation = " + Arrays.stream(ILSSmallPerturbationResults).average().getAsDouble());
            if (EXECUTE_ILS_BIG_PERTURBATION)
                System.out.println("Mean result for ILS big random perturbation = " + Arrays.stream(ILSBigPerturbationResults).average().getAsDouble());
            if (EXECUTE_ILS_BIG_PERTURBATION)
                System.out.println("Mean result for ILS big heuristic perturbation = " + Arrays.stream(ILSBigHeuristicPerturbationResults).average().getAsDouble());
            if (EXECUTE_EVOLUTIONARY)
                System.out.println("Mean result for Evolutionary = " + Arrays.stream(EvolutionaryResults).average().getAsDouble());

            if (EXECUTE_GREEDY_NAIVE)
                System.out.println("Max result for naive greedy = " + Arrays.stream(naiveGreedyResults).max().getAsDouble());
            if (EXECUTE_GREEDY_RANDOM)
                System.out.println("Max result for random greedy = " + Arrays.stream(randomGreedyResults).max().getAsDouble());
            if (EXECUTE_STEEPEST_NAIVE)
                System.out.println("Max result for naive steepest = " + Arrays.stream(naiveSteepestResults).max().getAsDouble());
            if (EXECUTE_STEEPEST_RANDOM)
                System.out.println("Max result for random steepest = " + Arrays.stream(randomSteepestResults).max().getAsDouble());
            if (EXECUTE_STEEPEST_CANDIDATE)
                System.out.println("Max result for naive steepest candidate = " + Arrays.stream(naiveSteepestCandidateResults).max().getAsDouble());
            if (EXECUTE_STEEPEST_CACHE)
                System.out.println("Max result for naive steepest cache = " + Arrays.stream(naiveSteepestCacheResults).max().getAsDouble());
            if (EXECUTE_STEEPEST_CANDIDATE_CACHE)
                System.out.println("Max result for naive steepest candidate + cache = " + Arrays.stream(naiveSteepestCandidateCacheResults).max().getAsDouble());
            if (EXECUTE_MSLS)
                System.out.println("Max result for MSLS = " + Arrays.stream(MSLSResults).max().getAsDouble());
            if (EXECUTE_ILS_SMALL_PERTURBATION)
                System.out.println("Max result for ILS small perturbation = " + Arrays.stream(ILSSmallPerturbationResults).max().getAsDouble());
            if (EXECUTE_ILS_BIG_PERTURBATION)
                System.out.println("Max result for ILS big random perturbation = " + Arrays.stream(ILSBigPerturbationResults).max().getAsDouble());
            if (EXECUTE_ILS_BIG_PERTURBATION)
                System.out.println("Max result for ILS big heuristic perturbation = " + Arrays.stream(ILSBigHeuristicPerturbationResults).max().getAsDouble());
            if (EXECUTE_EVOLUTIONARY)
                System.out.println("Max result for Evolutionary = " + Arrays.stream(EvolutionaryResults).max().getAsDouble());

            System.out.println("TIMING:");
            if (EXECUTE_GREEDY_NAIVE)
                System.out.println("Min time for naive greedy = " + Arrays.stream(naiveGreedyTimes).min().getAsDouble() / 1_000_000_000.0);
            if (EXECUTE_GREEDY_RANDOM)
                System.out.println("Min time for random greedy = " + Arrays.stream(randomGreedyTimes).min().getAsDouble() / 1_000_000_000.0);
            if (EXECUTE_STEEPEST_NAIVE)
                System.out.println("Min time for naive steepest = " + Arrays.stream(naiveSteepestTimes).min().getAsDouble() / 1_000_000_000.0);
            if (EXECUTE_STEEPEST_RANDOM)
                System.out.println("Min time for random steepest = " + Arrays.stream(randomSteepestTimes).min().getAsDouble() / 1_000_000_000.0);
            if (EXECUTE_STEEPEST_CANDIDATE)
                System.out.println("Min time for naive steepest candidate = " + Arrays.stream(naiveSteepestCandidateTimes).min().getAsDouble() / 1_000_000_000.0);
            if (EXECUTE_STEEPEST_CACHE)
                System.out.println("Min time for naive steepest cache = " + Arrays.stream(naiveSteepestCacheTimes).min().getAsDouble() / 1_000_000_000.0);
            if (EXECUTE_STEEPEST_CANDIDATE_CACHE)
                System.out.println("Min time for naive steepest candidate + cache = " + Arrays.stream(naiveSteepestCandidateCacheTimes).min().getAsDouble() / 1_000_000_000.0);
            if (EXECUTE_MSLS)
                System.out.println("Min time for MSLS = " + Arrays.stream(MSLSTimes).min().getAsDouble() / 1_000_000_000.0);
            if (EXECUTE_ILS_SMALL_PERTURBATION)
                System.out.println("Min time for ILS small perturbation = " + Arrays.stream(ILSSmallPerturbationTimes).min().getAsDouble() / 1_000_000_000.0);
            if (EXECUTE_ILS_BIG_PERTURBATION)
                System.out.println("Min time for ILS big random perturbation = " + Arrays.stream(ILSBigPerturbationTimes).min().getAsDouble() / 1_000_000_000.0);
            if (EXECUTE_ILS_BIG_PERTURBATION)
                System.out.println("Min time for ILS big heuristic perturbation = " + Arrays.stream(ILSBigHeuristicPerturbationTimes).min().getAsDouble() / 1_000_000_000.0);
            if (EXECUTE_EVOLUTIONARY)
                System.out.println("Min time for Evolutionary = " + Arrays.stream(EvolutionaryTimes).min().getAsDouble() / 1_000_000_000.0);

            if (EXECUTE_GREEDY_NAIVE)
                System.out.println("Mean time for naive greedy = " + Arrays.stream(naiveGreedyTimes).average().getAsDouble() / 1_000_000_000.0);
            if (EXECUTE_GREEDY_RANDOM)
                System.out.println("Mean time for random greedy = " + Arrays.stream(randomGreedyTimes).average().getAsDouble() / 1_000_000_000.0);
            if (EXECUTE_STEEPEST_NAIVE)
                System.out.println("Mean time for naive steepest = " + Arrays.stream(naiveSteepestTimes).average().getAsDouble() / 1_000_000_000.0);
            if (EXECUTE_STEEPEST_RANDOM)
                System.out.println("Mean time for random steepest = " + Arrays.stream(randomSteepestTimes).average().getAsDouble() / 1_000_000_000.0);
            if (EXECUTE_STEEPEST_CANDIDATE)
                System.out.println("Mean time for naive steepest candidate = " + Arrays.stream(naiveSteepestCandidateTimes).average().getAsDouble() / 1_000_000_000.0);
            if (EXECUTE_STEEPEST_CACHE)
                System.out.println("Mean time for naive steepest cache = " + Arrays.stream(naiveSteepestCacheTimes).average().getAsDouble() / 1_000_000_000.0);
            if (EXECUTE_STEEPEST_CANDIDATE_CACHE)
                System.out.println("Mean time for naive steepest candidate + cache = " + Arrays.stream(naiveSteepestCandidateCacheTimes).average().getAsDouble() / 1_000_000_000.0);
            if (EXECUTE_MSLS)
                System.out.println("Mean time for MSLS = " + Arrays.stream(MSLSTimes).average().getAsDouble() / 1_000_000_000.0);
            if (EXECUTE_ILS_SMALL_PERTURBATION)
                System.out.println("Mean time for ILS small perturbation = " + Arrays.stream(ILSSmallPerturbationTimes).average().getAsDouble() / 1_000_000_000.0);
            if (EXECUTE_ILS_BIG_PERTURBATION)
                System.out.println("Mean time for ILS big random perturbation = " + Arrays.stream(ILSBigPerturbationTimes).average().getAsDouble() / 1_000_000_000.0);
            if (EXECUTE_ILS_BIG_PERTURBATION)
                System.out.println("Mean time for ILS big heuristic perturbation = " + Arrays.stream(ILSBigHeuristicPerturbationTimes).average().getAsDouble() / 1_000_000_000.0);
            if (EXECUTE_EVOLUTIONARY)
                System.out.println("Mean time for Evolutionary = " + Arrays.stream(EvolutionaryTimes).average().getAsDouble() / 1_000_000_000.0);

            if (EXECUTE_GREEDY_NAIVE)
                System.out.println("Max time for naive greedy = " + Arrays.stream(naiveGreedyTimes).max().getAsDouble() / 1_000_000_000.0);
            if (EXECUTE_GREEDY_RANDOM)
                System.out.println("Max time for random greedy = " + Arrays.stream(randomGreedyTimes).max().getAsDouble() / 1_000_000_000.0);
            if (EXECUTE_STEEPEST_NAIVE)
                System.out.println("Max time for naive steepest = " + Arrays.stream(naiveSteepestTimes).max().getAsDouble() / 1_000_000_000.0);
            if (EXECUTE_STEEPEST_RANDOM)
                System.out.println("Max time for random steepest = " + Arrays.stream(randomSteepestTimes).max().getAsDouble() / 1_000_000_000.0);
            if (EXECUTE_STEEPEST_CANDIDATE)
                System.out.println("Max time for naive steepest candidate = " + Arrays.stream(naiveSteepestCandidateTimes).max().getAsDouble() / 1_000_000_000.0);
            if (EXECUTE_STEEPEST_CACHE)
                System.out.println("Max time for naive steepest cache = " + Arrays.stream(naiveSteepestCacheTimes).max().getAsDouble() / 1_000_000_000.0);
            if (EXECUTE_STEEPEST_CANDIDATE_CACHE)
                System.out.println("Max time for naive steepest candidate + cache = " + Arrays.stream(naiveSteepestCandidateCacheTimes).max().getAsDouble() / 1_000_000_000.0);
            if (EXECUTE_MSLS)
                System.out.println("Max time for MSLS = " + Arrays.stream(MSLSTimes).max().getAsDouble() / 1_000_000_000.0);
            if (EXECUTE_ILS_SMALL_PERTURBATION)
                System.out.println("Max time for ILS small perturbation = " + Arrays.stream(ILSSmallPerturbationTimes).max().getAsDouble() / 1_000_000_000.0);
            if (EXECUTE_ILS_BIG_PERTURBATION)
                System.out.println("Max time for ILS big random perturbation = " + Arrays.stream(ILSBigPerturbationTimes).max().getAsDouble() / 1_000_000_000.0);
            if (EXECUTE_ILS_BIG_PERTURBATION)
                System.out.println("Max time for ILS big heuristic perturbation = " + Arrays.stream(ILSBigHeuristicPerturbationTimes).max().getAsDouble() / 1_000_000_000.0);
            if (EXECUTE_EVOLUTIONARY)
                System.out.println("Max time for Evolutionary = " + Arrays.stream(EvolutionaryTimes).max().getAsDouble() / 1_000_000_000.0);
        }
    }

    /**
     * Change assignment to connection on graph
     *
     * @param algorithmResults Assignment to group
     * @param distanceMatrix   Distance matrix
     * @return Connections on graph (paths)
     */
    private HashSet<ArrayList<PointsPath>> castLocalSearchToMSTGraph(HashMap<Integer, HashSet<Integer>> algorithmResults, double[][] distanceMatrix) {
        HashSet<ArrayList<PointsPath>> groupsWithPaths = new HashSet<>();
        PrimSolver solver = new PrimSolver();

        for (Map.Entry<Integer, HashSet<Integer>> res : algorithmResults.entrySet()) {
            solver.construct(res.getValue().stream().mapToInt(Integer::intValue).toArray(), distanceMatrix);
            groupsWithPaths.add(solver.getPath());
        }

        return groupsWithPaths;
    }

    /**
     * Change assignment to connection on graph
     *
     * @param algorithmResults Assignment to group
     * @param distanceMatrix   Distance matrix
     * @return Connections on graph (paths)
     */
    private HashSet<ArrayList<PointsPath>> castLocalSearchToConnectionGraph(HashMap<Integer, HashSet<Integer>> algorithmResults, double[][] distanceMatrix) {
        HashSet<ArrayList<PointsPath>> groupsWithPaths = new HashSet<>();

        for (Map.Entry<Integer, HashSet<Integer>> entry : algorithmResults.entrySet()) {
            ArrayList<PointsPath> path = new ArrayList<>();
            int len = entry.getValue().size();
            int ind_i, ind_j;
            int[] indexes = entry.getValue().stream().mapToInt(Integer::intValue).toArray();

            for (int i = 0; i < len; i++) {
                ind_i = indexes[i];

                for (int j = i + 1; j < len; j++) {
                    ind_j = indexes[j];
                    path.add(new PointsPath(ind_i, ind_j, distanceMatrix[ind_i][ind_j]));
                }
            }

            groupsWithPaths.add(path);
        }

        return groupsWithPaths;
    }

    /**
     * Naive clustering algorithm
     *
     * @param distanceMatrix   Distance matrix
     * @param startIndexesList Start assignment (indexes)
     * @param coordinates      All points list
     * @return Naive assignment to groups
     */
    private HashMap<Integer, HashSet<Integer>> naiveAlgorithm(double[][] distanceMatrix, ArrayList<Integer> startIndexesList, ArrayList<PointCoordinates> coordinates) {
        // k-means with static center
        HashMap<Integer, HashSet<Integer>> elementsWithAssignmentToGroups = new HashMap<>();

        // Initialize groups
        for (int index : startIndexesList) {
            elementsWithAssignmentToGroups.put(index, new HashSet<>());
        }

        // Assign each point to group
        for (PointCoordinates point : coordinates) {
            int ID = point.getID();
            int selectedGroupIndex = 0;
            double minDistanceValue = Double.MAX_VALUE;

            for (int centerPointIndex : startIndexesList) {
                // Get distance from array
                double distance = distanceMatrix[centerPointIndex][ID];

                // Check distance is smaller than current stored - if yes => update index
                if (distance < minDistanceValue) {
                    minDistanceValue = distance;
                    selectedGroupIndex = centerPointIndex;
                }
            }
            // Add point to selected group
            elementsWithAssignmentToGroups.get(selectedGroupIndex).add(ID);
        }

        return elementsWithAssignmentToGroups;
    }

    /**
     * Save similarity result to CSV file
     *
     * @param similarity          List of similarities
     * @param fileName            Output file path
     * @param randomGreedyResults Results
     */
    private void saveCsv(int[] similarity, String fileName, double[] randomGreedyResults) {
        try (PrintWriter writer = new PrintWriter(new File(fileName))) {
            StringBuilder sb = new StringBuilder();
            sb.append("test_number");
            sb.append(',');
            sb.append("similarity");
            sb.append(',');
            sb.append("value");
            sb.append('\n');

            for (int i = 0; i < TESTS_NUMBER; i++) {
                sb.append(i);
                sb.append(',');
                sb.append(similarity[i]);
                sb.append(',');
                sb.append(randomGreedyResults[i]);
                sb.append('\n');
            }
            writer.write(sb.toString());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
