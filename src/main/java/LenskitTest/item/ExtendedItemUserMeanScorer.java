package LenskitTest.item;/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.Collections;
import static java.util.Collections.emptyList;
import javax.inject.Inject;
import org.grouplens.lenskit.baseline.MeanDamping;
import org.grouplens.lenskit.basic.AbstractItemScorer;
import org.grouplens.lenskit.data.dao.UserEventDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.history.History;
import org.grouplens.lenskit.data.history.RatingVectorUserHistorySummarizer;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;

/**
 *
 * @author Responsable
 */
public class ExtendedItemUserMeanScorer extends AbstractItemScorer {

    private final UserEventDAO dao;
    private final ItemMeanModel model;
    private final double userDamping;
    
    @Inject
    public ExtendedItemUserMeanScorer(UserEventDAO dao, ItemMeanModel inModel, @MeanDamping double inUserDamping) {
        this.dao=dao;
        this.model = inModel;
        this.userDamping = inUserDamping;
    }
    
    protected double computeUserOffset (SparseVector ratings) {
        if (ratings.isEmpty()) {
            return 0;
        }
        MutableSparseVector v = ratings.mutableCopy();
        v.add(-model.getGlobalMean());
        v.subtract(model.getItemOffsets());
        return (v.sum() / v.size() + userDamping);
    }
    
    public void score(long user, MutableSparseVector scores) {
        UserHistory<Rating> profile = dao.getEventsForUser(user, Rating.class);
        if (profile == null) {
            profile = History.forUser(user);
        }
        SparseVector vector = RatingVectorUserHistorySummarizer.makeRatingVector(profile);
        double meanOffset = computeUserOffset(vector);
        scores.fill(model.getGlobalMean());
        scores.add(model.getItemOffsets());
        scores.add(meanOffset);
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
}
