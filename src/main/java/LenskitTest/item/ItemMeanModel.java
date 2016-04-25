package LenskitTest.item;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.Serializable;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.vectors.ImmutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;

/**
 *
 * @author Responsable
 */
@Shareable
@DefaultProvider(ItemMeanModelBuilder.class)      
class ItemMeanModel implements Serializable{

    private final double globalMean;
    private final ImmutableSparseVector itemOffsets;
    
    public ItemMeanModel(double global, SparseVector items) {
        itemOffsets = items.immutable();
        globalMean = global;
    }
    
    public double getGlobalMean() {
        return globalMean;
    }

    public ImmutableSparseVector getItemOffsets() {
        return itemOffsets;
    }
    
}
