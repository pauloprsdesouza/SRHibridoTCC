package Model;

public class Movie100K {
	
	private int idMoieLens;
	private String name;
	private String uriDbpedia;
	
	
	public Movie100K(int idMoieLens, String name, String uriDbpedia) {
		super();
		this.idMoieLens = idMoieLens;
		this.name = name;
		this.uriDbpedia = uriDbpedia;
	}


	public int getIdMoieLens() {
		return idMoieLens;
	}


	public void setIdMoieLens(int idMoieLens) {
		this.idMoieLens = idMoieLens;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getUriDbpedia() {
		return uriDbpedia;
	}


	public void setUriDbpedia(String uriDbpedia) {
		this.uriDbpedia = uriDbpedia;
	}


}
