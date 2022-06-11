package filmRecommender;

import java.util.ArrayList;
import java.util.List;
import net.librec.conf.Configuration;
import net.librec.data.model.TextDataModel;
import net.librec.eval.RecommenderEvaluator;
import net.librec.eval.rating.MAEEvaluator;
import net.librec.eval.rating.RMSEEvaluator;
import net.librec.eval.ranking.*;
import net.librec.filter.GenericRecommendedFilter;
import net.librec.recommender.Recommender;
import net.librec.recommender.RecommenderContext;
import net.librec.recommender.cf.ItemKNNRecommender;
import net.librec.recommender.cf.ItemKNNRecommenderLDSD;
import net.librec.recommender.item.RecommendedItem;
import net.librec.similarity.CosineSimilarity;
import net.librec.similarity.RecommenderSimilarity;

public class FilmRecommenderCosine {

	public static void main(String[] args) throws Exception {

		// build data model
		String dataDir = "D:/Projects/SRHibridoTCC/librec-librec-src-v2.0/data";
		Configuration conf = new Configuration();
		conf.set("dfs.data.dir", dataDir);
		TextDataModel dataModel = new TextDataModel(conf);
		dataModel.buildDataModel(); // ler formato dos dados e o cmainho do arquivo
		// build recommender context
		RecommenderContext recommenderContext = new RecommenderContext(conf, dataModel);

		System.out.println("1 Passou");
		conf.set("rec.recommender.similarities", "item");
		conf.set("rec.similarity.class", "cos");
		RecommenderSimilarity similarity = new CosineSimilarity();
		similarity.buildSimilarityMatrix(dataModel);
		recommenderContext.setSimilarity(similarity);// seta a similaridade somente
		
		System.out.println("2 Passou");
		Recommender recommender = new ItemKNNRecommenderLDSD();
		recommender.setContext(recommenderContext);
		recommender.recommend(recommenderContext); 

		List<RecommendedItem> recommendedItemList = recommender.getRecommendedList();

		System.out.println("3 Passou");
		// set USER id list of filter
		List<String> userIdList = new ArrayList<>();
		userIdList.add("757");

		System.out.println("4 Passou");
		GenericRecommendedFilter genericFilter = new GenericRecommendedFilter();
		genericFilter.setUserIdList(userIdList);
		recommendedItemList = genericFilter.filter(recommendedItemList);

		// print filter result
		System.out.println("Size: " + recommendedItemList.size());
		for (RecommendedItem recommendedItem : recommendedItemList) {
			System.out.println("user:" + recommendedItem.getUserId() + " " + "item:" + recommendedItem.getItemId() + " "
					+ "value:" + recommendedItem.getValue() + "\n");
		}

		RecommenderEvaluator evaluator = new RMSEEvaluator();
		RecommenderEvaluator evaluatorMAE = new MAEEvaluator();
		
		System.out.println("--------------------- ItemKNNRecommenderLDSD ----------------------------");
		System.out.println("RMSE:" + recommender.evaluate(evaluator));
		System.out.println("MAE:" + recommender.evaluate(evaluatorMAE));
		

	}
}
