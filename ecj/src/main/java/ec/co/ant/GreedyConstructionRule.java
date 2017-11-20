/*
  Copyright 2017 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.co.ant;

import ec.EvolutionState;
import ec.co.ConstructiveProblemForm;
import ec.util.Misc;
import ec.util.Parameter;
import ec.vector.IntegerVectorIndividual;
import ec.vector.IntegerVectorSpecies;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * A construction rule that ignores pheromones and selects the best local move 
 * at each step.
 * 
 * @author Eric O. Scott
 */
public class GreedyConstructionRule implements ConstructionRule
{
    public final static String P_MINIMIZE = "minimize";
    private boolean minimize;
    
    @Override
    public void setup(final EvolutionState state, final Parameter base)
    {
        assert(state != null);
        assert(base != null);
        minimize = state.parameters.getBoolean(base.push(P_MINIMIZE), null, true);
        assert(repOK());
    }

    @Override
    public IntegerVectorIndividual constructSolution(final EvolutionState state, final int subpop, final int startNode, final PheromoneMatrix pheromones)
    {
        assert(state != null);
        assert(subpop >= 0);
        assert(subpop < state.population.subpops.size());
        assert(pheromones != null);
        assert(startNode >= 0);
        assert(startNode < pheromones.numNodes());
        assert(state.evaluator.p_problem instanceof ConstructiveProblemForm);
        assert(state.population.subpops.get(subpop).species instanceof IntegerVectorSpecies);
        
        final ConstructiveProblemForm problem = (ConstructiveProblemForm) state.evaluator.p_problem;
        final IntegerVectorSpecies species = (IntegerVectorSpecies) state.population.subpops.get(subpop).species;
        
        assert(problem != null);
        assert(pheromones.numNodes() == problem.numComponents());
        assert(species != null);
        
        // Prepare data structures
        final int length = pheromones.numNodes();
        final List<Integer> path = new ArrayList<Integer>();
        final Collection<Integer> allowedMoves = new HashSet<Integer>();
        for (int i = 0; i < length; i++)
            allowedMoves.add(i);
        
        // Constructively build a new individual
        int currentNode = startNode;
        path.add(currentNode);
        allowedMoves.remove(currentNode);
        while (!allowedMoves.isEmpty())
            {
            currentNode = bestMove(currentNode, pheromones, problem, allowedMoves);
            path.add(currentNode);
            allowedMoves.remove(currentNode);
            }
        
        final IntegerVectorIndividual ind = (IntegerVectorIndividual) species.newIndividual(state, 0);
        ind.setGenome(path.toArray());
        assert(repOK());
        return ind;
    }
    
    /** Greedily select the next move. */
    private int bestMove(final int currentNode, final PheromoneMatrix pheromones, final ConstructiveProblemForm problem, final Collection<Integer> allowedMoves)
    {
        assert(pheromones != null);
        assert(problem != null);
        assert(pheromones.numNodes() == problem.numComponents());
        assert(allowedMoves != null);
        assert(!allowedMoves.isEmpty());
        assert(!Misc.containsNulls(allowedMoves));
        
        double bestCost = minimize ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
        int best = -1;
        for (final int move : allowedMoves)
            {
            final double cost = problem.desireability(currentNode, move);
            if (minimize ? cost <= bestCost : cost >= bestCost)
                {
                bestCost = cost;
                best = move;
                }
            }
        assert(best >= 0);
        return best;
    }
    
    /** Representation invariant, used for verification.
     * 
     * @return true if the class is found to be in an erroneous state.
     */
    public final boolean repOK()
    {
        return P_MINIMIZE != null
                && !P_MINIMIZE.isEmpty();
    }
}