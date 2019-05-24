package filmRecommender;

import java.util.ArrayList;
import java.util.List;
import net.librec.conf.Configuration;
import net.librec.data.model.TextDataModel;
import net.librec.eval.RecommenderEvaluator;
import net.librec.eval.rating.MAEEvaluator;
import net.librec.eval.rating.RMSEEvaluator;
import net.librec.filter.GenericRecommendedFilter;
import net.librec.recommender.Recommender;
import net.librec.recommender.RecommenderContext;
import net.librec.recommender.baseline.UserClusterRecommender;
import net.librec.recommender.cf.ItemKNNRecommender;
import net.librec.recommender.item.RecommendedItem;
import net.librec.similarity.CosineSimilarity;
import net.librec.similarity.PCCSimilarity;
import net.librec.similarity.RecommenderSimilarity;

public class FilmRecommender {

	public static void main(String[] args) throws Exception {

		// build data model
		String dataDir = "C:/Users/Ramon/neon/workspace/librec-librec-src-v2.0/data";
		Configuration conf = new Configuration();
		conf.set("dfs.data.dir", dataDir);
		TextDataModel dataModel = new TextDataModel(conf);
		dataModel.buildDataModel();
		// build recommender context
		RecommenderContext recommenderContext = new RecommenderContext(conf, dataModel);

		// build PCC similarity
		conf.set("rec.recommender.similarity.key", "item");
		RecommenderSimilarity recommenderSimilarity = new PCCSimilarity();
		recommenderSimilarity.buildSimilarityMatrix(dataModel);
		recommenderContext.setSimilarity(recommenderSimilarity);

		// build KNN recommender
		conf.set("rec.neighbors.knn.number", "5");
		Recommender recommender = new ItemKNNRecommender();
		recommender.setContext(recommenderContext);
		// run recommender algorithm
		recommender.recommend(recommenderContext);

		// evaluate the recommended result with RMSEE
		RecommenderEvaluator recommenderEvaluator = new RMSEEvaluator();
		System.out.println("RMSE:" + recommender.evaluate(recommenderEvaluator));
		
		// set USER id list of filter
		List<String> userIdList = new ArrayList<>();
		userIdList.add("1");
		userIdList.add("15");
		userIdList.add("666");
		
		// filter the recommended result by USER
		List<RecommendedItem> recommendedItemList =
		recommender.getRecommendedList();
		GenericRecommendedFilter genericFilter = new GenericRecommendedFilter();
		genericFilter.setUserIdList(userIdList);
		recommendedItemList = genericFilter.filter(recommendedItemList); 
		
		// print filter result by USER
		for (RecommendedItem recommendedItem : recommendedItemList) {
		 System.out.println( 
		 "user:" + recommendedItem.getUserId() + " " +
		 "item:" + recommendedItem.getItemId() + " " +
		 "value:" + recommendedItem.getValue()
		 );
		}
/*
		// build COSINE similarity
		conf.set("rec.recommender.similarities","user");
		conf.set("rec.similarity.class","cos");
		RecommenderSimilarity similarity = new CosineSimilarity();
		similarity.buildSimilarityMatrix(dataModel);
		recommenderContext.setSimilarity(similarity);
		
		// build USER CLUSTER recommender
		conf.set("rec.recommender.class", "usercluster");
		conf.set("rec.factory.number", "10");
		conf.set("rec.iterator.maximum", "20");
		recommender = new UserClusterRecommender();
		recommender.setContext(recommenderContext);*/
		
		// evaluate the recommended result with MAE
		conf.set("rec.recommender.isranking", "true");
		conf.set("rec.eval.enable", "true");
		conf.set("rec.eval.class", "mae");
		conf.set("rec.recommender.ranking.topn", "10");
		RecommenderEvaluator evaluator = new MAEEvaluator();
		System.out.println("MAE:" + recommender.evaluate(evaluator));
		
		// set ITEM id list of filter
		List<String> itemIdList = new ArrayList<>();
		itemIdList.add("5");
		itemIdList.add("25");
		itemIdList.add("125");
		
		// filter the recommended result by ITEM
		List<RecommendedItem> recommendedItemList2 =
		recommender.getRecommendedList();
		GenericRecommendedFilter genericFilter2 = new GenericRecommendedFilter();
		genericFilter2.setItemIdList(itemIdList);
		recommendedItemList2 = genericFilter2.filter(recommendedItemList2);
		
		// print filter result by Item
				for (RecommendedItem recommendedItem : recommendedItemList2) {
				 System.out.println(
				 "user:" + recommendedItem.getUserId() + " " +
				 "item:" + recommendedItem.getItemId() + " " +
				 "value:" + recommendedItem.getValue()
				 );
				}
		
	}
}
