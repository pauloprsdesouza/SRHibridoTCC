package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import Model.Movie100K;


public class DBFunctions {
	
	public void saveMovie(Movie100K movie) {
		Connection conn = DBConnection.getConnection();
		PreparedStatement ps = null;

		try {
			try {
				String query = "INSERT INTO `movies_movielens`.`movie_100_k` (`idMovieLens`, `title`, `uri_dbpedia`) VALUES (?, ?, ?)";
				ps = conn.prepareStatement(query);
				ps.setInt(1, movie.getIdMoieLens());
				ps.setString(2, movie.getName());
				ps.setString(3, movie.getUriDbpedia());
				ps.execute();
				ps.close();
				conn.close();
			} catch (Exception e) {
				System.out.println("Erro ao Salvar!");
			}

		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public String findUriMovie(int idMovie) {
		String uri = "";

		try {
			Connection conn = DBConnection.getConnection();
			String query = "SELECT * from `movies_movielens`.`movie_100_k` where `idMovieLens`= ? ";
			PreparedStatement ps = conn.prepareStatement(query);
			ps.setInt(1, idMovie);
			ResultSet rs = ps.executeQuery();
			while (rs != null && rs.next()) {
				uri = rs.getString(3);
			}
			closeQuery(conn, ps);
		} catch (SQLException ex) {
			System.out.println("Bd 2x");
			try {
				Connection conn = DBConnection.getConnection();
				String query = "SELECT * from `movies_movielens`.`movie_100_k` where `idMovieLens`= ? ";
				PreparedStatement ps = conn.prepareStatement(query);
				ps.setInt(1, idMovie);
				ResultSet rs = ps.executeQuery();
				while (rs != null && rs.next()) {
					uri = rs.getString(3);
				}
				closeQuery(conn, ps);
			} catch (SQLException ex2) {
				System.out.println("Vazio");
				uri="";
			}
			
		}
		return uri;
		
	}
	
	public static void closeQuery(Connection conn, PreparedStatement ps) {
		try {
			ps.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public Double findUriMovieSimilarityLDSD(String resourceA, String resourceB) {
		Double similarity = - 1.0;

		try {
			Connection conn = DBConnection.getConnection();
			String query = "SELECT * from `movies_movielens`.`similarity_ldsd` where `resourceA`= ? AND `resourceB`= ?";
			PreparedStatement ps = conn.prepareStatement(query);
			ps.setString(1, resourceA);
			ps.setString(2, resourceB);
			ResultSet rs = ps.executeQuery();
			while (rs != null && rs.next()) {
				similarity = rs.getDouble(3);
			}
			closeQuery(conn, ps);
		} catch (SQLException ex) {
			System.out.println("Bd 2x");
			try {
				Connection conn = DBConnection.getConnection();
				String query = "SELECT * from `movies_movielens`.`similarity_ldsd` where `resourceA`= ? AND `resourceB`= ?";
				PreparedStatement ps = conn.prepareStatement(query);
				ps.setString(1, resourceA);
				ps.setString(2, resourceB);
				ResultSet rs = ps.executeQuery();
				while (rs != null && rs.next()) {
					similarity = rs.getDouble(3);
				}
				closeQuery(conn, ps);
			} catch (SQLException ex2) {
				System.out.println("Vazio");
				similarity = - 1.0;
			}
			
		}
		return similarity;
		
	}
	
	public void saveMovieSimilarityLDSD(String resourceA, String resourceB, Double similarity) {
		Connection conn = DBConnection.getConnection();
		PreparedStatement ps = null;

		try {
			try {
				String query = "INSERT INTO `movies_movielens`.`similarity_ldsd` (`resourceA`, `resourceB`, `similarity`) VALUES (?, ?, ?)";
				ps = conn.prepareStatement(query);
				ps.setString(1, resourceA);
				ps.setString(2, resourceB);
				ps.setDouble(3, similarity);
				ps.execute();
				ps.close();
				conn.close();
			} catch (Exception e) {
				System.out.println("Erro ao Salvar!");
			}

		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
}
