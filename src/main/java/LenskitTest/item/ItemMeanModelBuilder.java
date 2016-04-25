package LenskitTest.item;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import javax.inject.Inject;
import javax.inject.Provider;
import org.grouplens.lenskit.baseline.MeanDamping;
import org.grouplens.lenskit.core.Transient;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.pref.Preference;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;

/**
 *
 * @author Responsable
 */
public class ItemMeanModelBuilder implements Provider<ItemMeanModel> {
    
    private double damping = 0;
    private EventDAO dao;
    
    @Inject
    public ItemMeanModelBuilder(@Transient EventDAO dao, @MeanDamping double d) {
        this.damping = d;
        this.dao = dao;
    }
    
    public ItemMeanModel get() {
        double total = 0.0;
        int count = 0;
        Long2DoubleMap itemRatingSums = new Long2DoubleOpenHashMap();
        itemRatingSums.defaultReturnValue(0.0);
        Long2IntMap itemRatingCounts = new Long2IntOpenHashMap();
        itemRatingCounts.defaultReturnValue(0);
        Cursor<Rating> ratings = dao.streamEvents(Rating.class);
        try {
            for (Rating rating : ratings) {
                Preference pref = rating.getPreference();
                if (pref == null) {
                    continue;
                }
                long i = pref.getItemId();
                double v = pref.getValue();
                total += v;
                count ++;
                itemRatingSums.put(i, v+itemRatingSums.get(i));
                itemRatingCounts.put(i, 1+itemRatingCounts.get(i));
            }
        } finally {
            ratings.close();
        }
        final double mean = count > 0 ? total / count : 0;
        MutableSparseVector vector = MutableSparseVector.create(itemRatingCounts.keySet());
        for (VectorEntry e : vector.fast(VectorEntry.State.EITHER)) {
            final long iid = e.getKey();
            final double itemCount = itemRatingCounts.get(iid) + damping;
            final double itemTotal = itemRatingSums.get(iid) + damping * mean;
            if (itemCount > 0) {
                vector.set(e,itemTotal / itemCount - mean);
            }
        }
        return new ItemMeanModel(mean,vector.freeze());
    }
}
