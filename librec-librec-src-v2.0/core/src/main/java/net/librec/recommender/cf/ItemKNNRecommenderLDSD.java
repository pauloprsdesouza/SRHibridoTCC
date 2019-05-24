/**
 * Copyright (C) 2016 LibRec
 * <p>
 * This file is part of LibRec.
 * LibRec is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * LibRec is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with LibRec. If not, see <http://www.gnu.org/licenses/>.
 */
package net.librec.recommender.cf;

import net.librec.annotation.ModelData;
import net.librec.common.LibrecException;
import net.librec.math.structure.DenseVector;
import net.librec.math.structure.SparseVector;
import net.librec.math.structure.SymmMatrix;
import net.librec.math.structure.VectorEntry;
import net.librec.recommender.AbstractRecommender;
import net.librec.util.Lists;
import similarity.LDSD;
import similarity.LDSD_LOD;

import java.net.ConnectException;
import java.util.*;
import java.util.Map.Entry;

import org.apache.http.HttpException;

import database.DBFunctions;

/**
 * ItemKNNRecommender
 *
 * @author WangYuFeng and Keqiang Wang
 */
@ModelData({ "isRanking", "knn", "itemMeans", "trainMatrix", "similarityMatrix" })
public class ItemKNNRecommenderLDSD extends AbstractRecommender {
	private int knn;
	private DenseVector itemMeans;
	private SymmMatrix similarityMatrix;
	private List<Map.Entry<Integer, Double>>[] itemSimilarityList;
	private int currentUserIdx = -1;
	private Set<Integer> currentItemIdxSet;

	/**
	 * (non-Javadoc)
	 *
	 * @see net.librec.recommender.AbstractRecommender#setup()
	 */
	@Override
	protected void setup() throws LibrecException {
		super.setup();
		knn = conf.getInt("rec.neighbors.knn.number", 50);
		similarityMatrix = context.getSimilarity().getSimilarityMatrix();
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see net.librec.recommender.AbstractRecommender#trainModel()
	 */
	@Override
	protected void trainModel() throws LibrecException {
		itemMeans = new DenseVector(numItems);
		int numRates = trainMatrix.size();
		double globalMean = trainMatrix.sum() / numRates;
		for (int itemIdx = 0; itemIdx < numItems; itemIdx++) {
			SparseVector userRatingVector = trainMatrix.column(itemIdx);
			itemMeans.set(itemIdx, userRatingVector.getCount() > 0 ? userRatingVector.mean() : globalMean);
		}
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see net.librec.recommender.AbstractRecommender#predict(int, int)
	 */
	public double predict(int userIdx, int itemIdx) throws LibrecException {
		// create itemSimilarityList if not exists
		if (!(null != itemSimilarityList && itemSimilarityList.length > 0)) {
			createItemSimilarityList();
		}

		if (currentUserIdx != userIdx) {
			System.out.println("userIdx: " + userIdx);
			currentItemIdxSet = trainMatrix.getColumnsSet(userIdx);
			currentUserIdx = userIdx;
		}

		// find a number of similar items
		List<Map.Entry<Integer, Double>> nns = new ArrayList<>();
		List<Map.Entry<Integer, Double>> simList = itemSimilarityList[itemIdx];

		int count = 0;
		for (Map.Entry<Integer, Double> itemRatingEntry : simList) {
			int similarItemIdx = itemRatingEntry.getKey();// Pega o filme similar em relação ao filme atual

			// Verifica se o usuário atual já assistiu o filme similar
			if (!currentItemIdxSet.contains(similarItemIdx)) {
				continue;
			}

			double sim = itemRatingEntry.getValue();

			if (isRanking) {
				nns.add(itemRatingEntry);
				count++;
			} else if (sim > 0) {
				nns.add(itemRatingEntry);
				count++;
			}
			if (count == knn) {
				break;
			}
		}
		if (nns.size() == 0) {
			return isRanking ? 0 : globalMean;
		}
		if (isRanking) {
			double sum = 0.0d;
			for (Entry<Integer, Double> itemRatingEntry : nns) {
				sum += itemRatingEntry.getValue();
			} // System.out.println("sum "+sum);
			return sum;
		} else {
			// for rating prediction
			double sum = 0, ws = 0;
			for (Entry<Integer, Double> itemRatingEntry : nns) {
				int similarItemIdx = itemRatingEntry.getKey();

				DBFunctions dbFunctions = new DBFunctions();

				// Busca a uri dos filmes no banco de dados
				String uriItemIdx = dbFunctions.findUriMovie(itemIdx);
				String uriSimilarItemIdx = dbFunctions.findUriMovie(similarItemIdx);

				double sim = 0.0;

				// Verifica se os dois filmes possuem a uri
				if (!uriItemIdx.equals("") && !uriSimilarItemIdx.equals("")) {
					// System.out.println("itemIdx:"+itemIdx);
					/// System.out.println("similarItemIdx:"+similarItemIdx);

					// Pega a similaridade (LDSD) dos filmes no banco de dados
					Double simLDSD = dbFunctions.findUriMovieSimilarityLDSD(uriItemIdx, uriSimilarItemIdx);

					if (simLDSD < 0.0) {
						// Se a similaridade for 0 testa o inverso
						simLDSD = dbFunctions.findUriMovieSimilarityLDSD(uriSimilarItemIdx, uriItemIdx);
						if (simLDSD < 0.0) {// Se a similaridade ainda for 0, calcula o LDSD buscando na DBPedia e
												// salva no banco de dados

							LDSD ldsd = new LDSD();
							LDSD_LOD ldsdlod = new LDSD_LOD();
							// System.out.println("itemRatingEntry.getValue():"+itemRatingEntry.getValue());
							simLDSD = ldsd.LDSDdirect(uriItemIdx, uriSimilarItemIdx);
							// System.out.println("simLDSD:"+simLDSD);
							
							dbFunctions.saveMovieSimilarityLDSD(uriItemIdx, uriSimilarItemIdx, simLDSD);
							

						}
					}

					double similarutylDSD = 1 - simLDSD;
					if (similarutylDSD != 0) {
						sim = itemRatingEntry.getValue() * similarutylDSD; // multiplica a similaridade do LibRec com a
																			// do LDSD
					} else {
						sim = itemRatingEntry.getValue();

					}
					// System.out.println("sim+LDSD: "+sim);
					// System.out.println("userIdx:"+userIdx);
				} else {
					// Caso um dos flmes não tenha a uri, fica a similaridade do LibRec
					sim = itemRatingEntry.getValue();
				}

				double rate = trainMatrix.get(userIdx, similarItemIdx); // avalição que o usuaio corrent atribuiu ao
																		// item similar
				// System.out.println("similarItemIdx: "+similarItemIdx+" rate: "+rate);
				sum += sim * (rate - itemMeans.get(similarItemIdx));
				ws += Math.abs(sim);
			}
			return ws > 0 ? itemMeans.get(itemIdx) + sum / ws : globalMean;
		}
	}

	/**
	 * Create itemSimilarityList.
	 */
	public void createItemSimilarityList() {// a cada interaçã eçe pega a linha de item imilares e joga em
											// iteemsimilaritylist, sendo que a cada poseoção de iteemsimilaritylist
											// contem uma lista como id o utem similar e o vlor de sim
		itemSimilarityList = new ArrayList[numItems];
		for (int itemIdx = 0; itemIdx < numItems; ++itemIdx) {
			SparseVector similarityVector = similarityMatrix.row(itemIdx);
			itemSimilarityList[itemIdx] = new ArrayList<>(similarityVector.size());
			Iterator<VectorEntry> simItr = similarityVector.iterator();
			while (simItr.hasNext()) {
				VectorEntry simVectorEntry = simItr.next();
				itemSimilarityList[itemIdx]
						.add(new AbstractMap.SimpleImmutableEntry<>(simVectorEntry.index(), simVectorEntry.get()));
			}
			Lists.sortList(itemSimilarityList[itemIdx], true);
		}
	}

}
