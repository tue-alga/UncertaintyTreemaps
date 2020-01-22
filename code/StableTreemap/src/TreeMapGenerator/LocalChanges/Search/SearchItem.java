/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TreeMapGenerator.LocalChanges.Search;

import TreeMapGenerator.LocalChanges.OrderEquivalenceGraph;
import TreeMapGenerator.LocalChanges.OrderEquivalentMaximalSegment;
import java.util.List;

/**
 *
 * @author msondag
 */
public class SearchItem implements Comparable<SearchItem>{

    public OrderEquivalenceGraph oeg;
    public int movesLeft;
    public List<OrderEquivalentMaximalSegment> innerMaximalSegments;
    public double maxAspectRatio;

    public SearchItem(OrderEquivalenceGraph oeg, int movesLeft, List<OrderEquivalentMaximalSegment> innerMaximalSegments, double maxAspectRatio) {
        this.oeg = oeg;
        this.movesLeft = movesLeft;
        this.innerMaximalSegments = innerMaximalSegments;
        this.maxAspectRatio = maxAspectRatio;
    }

    @Override
    public int compareTo(SearchItem o) {
        return Double.compare(maxAspectRatio, o.maxAspectRatio);
    }
}
