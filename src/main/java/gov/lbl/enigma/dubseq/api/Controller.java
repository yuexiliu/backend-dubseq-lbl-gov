package gov.lbl.enigma.dubseq.api;

import gov.lbl.enigma.dubseq.dao.GenesDao;
import gov.lbl.enigma.dubseq.dao.GenomeDao;
import gov.lbl.enigma.dubseq.model.*;
import gov.lbl.enigma.dubseq.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("api")
public class Controller {

    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Autowired
    private FScoreCollector fScoreCollector;

    @Autowired
    private GScoreCollector gScoreCollector;

    @Autowired
    private GenesDao genesDao;

    @Autowired
    private GenomeDao genomeDao;

    @Autowired
    private ExperimentsCollector experimentsCollector;

    @Autowired
    private GenesCollector genesCollector;

    @Autowired
    private LayoutCollector layoutCollector;

    @CrossOrigin
    @GetMapping("/fragview")
    public Collection<FragView> getFragmenrs(
            @RequestParam int posFrom,
            @RequestParam int posTo) throws IOException {
        return fScoreCollector.composeFragment(posFrom, posTo);
    }

    @CrossOrigin
    @GetMapping("/geneview")
    public Collection<GeneView> getGenes(
            @RequestParam int posFrom,
            @RequestParam int posTo) throws IOException {
        return gScoreCollector.composeGene(posFrom, posTo);
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * *
     * endpoints for landing page of Organisms
     * * * * * * * * * * * * * * * * * * * * * * * * * * * */
    @CrossOrigin
    @GetMapping("/organisms")
    public List<Map<String, Object>> getOrganisms(
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String type) {

        String QUERY = "select\n" +
                "\tg.genome_id,\n" +
                "\tg.\"name\",\n" +
                "\tg.\"size\",\n" +
                "\tg.ncbi_taxonomy_id,\n" +
                "\tg.phylum,\n" +
                "\t(select count(*) from gene g2 where g2.genome_id = g.genome_id),\n" +
                "\t(select count(*) from bagseq_library b where b.genome_id = g.genome_id) as library_count,\n" +
                "\t(select \n" +
                "\t\tcount(*) \n" +
                "\tfrom barseq_experiment e join bagseq_library b using(bagseq_library_id) where b.genome_id = g.genome_id) as experiment_count\n" +
                "from genome g";

        if (id != null) {
            QUERY = QUERY + " where g.genome_id = " + id;
        }
        return jdbcTemplate.queryForList(QUERY, new HashMap<String, Object>());
    }

    @CrossOrigin
    @GetMapping("/organisms/{id}/stats")
    public List<Map<String, Object>> getOrganismStats(@PathVariable long id) {

        String QUERY = "select \n" +
                "\tg.\"name\" as \"Name:\",\n" +
                "\tg.genome_id as \"Genome id:\",\n" +
                "\tg.\"size\" as \"Size:\",\n" +
                "\tg.ncbi_taxonomy_id as \"Taxonomy id:\",\n" +
                "\tg.phylum as \"Phylum:\",\n" +
                "\tg.gene_count as \"Gene count:\"\n" +
                "from genome g \n" +
                "where g.genome_id = " + id;


        return jdbcTemplate.queryForList(QUERY, new HashMap<>());
    }

    @CrossOrigin
    @GetMapping("/organisms/{id}/libraries")
    public List<Map<String, Object>> getOrganismLibraries(@PathVariable long id) {

        String QUERY = "select \n" +
                "\tbl.\"name\" as \"Name\",\n" +
                "\tbl.bagseq_library_id as \"id\",\t\n" +
                "\t(select count(*) from barseq_experiment be where bl.bagseq_library_id = be.bagseq_library_id ) as \"Experiments\",\n" +
                "\t(select count(*) from bagseq_fragment bf where bl.bagseq_library_id = bf.bagseq_library_id) as \"Fragments\"\n" +
                "from \n" +
                "\tbagseq_library bl inner join genome g on bl.genome_id = g.genome_id \n" +
                "where \n" +
                "\tg.genome_id = " + id;

        return jdbcTemplate.queryForList(QUERY, new HashMap<>());
    }

    @CrossOrigin
    @GetMapping("/organisms/{id}/experiments")
    public List<Map<String, Object>> getOrganismExperiments(@PathVariable long id) {

        String QUERY = "select \n" +
                "\tbe.\"name\",\n" +
                "\tbe.\"type\",\n" +
                "\tbgs.score_cnnls as \"cnnls score\"\n" +
                "from genome g \n" +
                "\tinner join bagseq_library bl on g.genome_id = bl.genome_id \n" +
                "\tinner join barseq_experiment be on bl.bagseq_library_id = be.bagseq_library_id \n" +
                "\tinner join barseq_gene_score bgs on be.barseq_experiment_id = bgs.barseq_experiment_id \n" +
                "where g.genome_id = " + id + "\n" +
                "order by bgs.score_ridge desc limit 10";


        return jdbcTemplate.queryForList(QUERY, new HashMap<>());
    }

    @CrossOrigin
    @GetMapping("/organisms/{id}/graph")
    public List<Map<String, Object>> getOrganismGraph(@PathVariable long id) {

        String QUERY = "select\n" +
                "be.type,\n" +
                "count(*)\n" +
                "from genome g\n" +
                "inner join bagseq_library bl on g.genome_id = bl.genome_id\n" +
                "inner join barseq_experiment be on bl.bagseq_library_id = be.bagseq_library_id\n" +
                "where g.genome_id = " + id + "\n" +
                "group by be.type";

        return jdbcTemplate.queryForList(QUERY, new HashMap<String, Object>());
    }

    @CrossOrigin
    @GetMapping("/organisms/{id}")
    public List<Map<String, Object>> getOrganisms(@PathVariable long id) throws IOException {

        return jdbcTemplate.queryForList("select genome_id, name from genome", new HashMap<String, Object>());
    }


    /* * * * * * * * * * * * * * * * * * * * * * * * * * * *
     * endpoints for landing page of Bagseq Library
     * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    // Basic statistics for Library by id.
    @CrossOrigin
    @GetMapping("/bagseq/{id}/stats")
    public List<Map<String, Object>> getBagSeq(@PathVariable long id) {

        return jdbcTemplate.queryForList("select \n" +
                "\tbl.name as \"Name:\",\n" +
                "\tbl.bagseq_library_id as \"Library id:\",\n" +
                "\t(select g.name from genome g where bl.genome_id = g.genome_id) as \"Host Name:\",\n" +
                "\tbl.host_genome_id as \"Host id:\",\n" +
                "\t(select count(*) from bagseq_fragment bf where bl.bagseq_library_id = bf.bagseq_library_id) as \"Fragment count:\",\n" +
                "\t(select count(*) from barseq_experiment be where bl.bagseq_library_id = be.bagseq_library_id) as \"Experiment count:\"\n" +
                "from bagseq_library bl \n" +
                "where bl.bagseq_library_id = " + id, new HashMap<>());
    }

    // the organism for which this library is made.
    @CrossOrigin
    @GetMapping("/bagseq/{id}/organism")
    public List<Map<String, Object>> getBagSeqOrganism(@PathVariable long id) {
        return jdbcTemplate.queryForList("select \n" +
                "\tg.name\n" +
                "from bagseq_library bl\n" +
                "inner join genome g on bl.genome_id = g.genome_id \n" +
                "where bl.bagseq_library_id = " + id, new HashMap<>());
    }


    // List of experiments performed on this library.
    @CrossOrigin
    @GetMapping("/bagseq/{id}/experiments")
    public List<Map<String, Object>> getBagSeqExperiments(@PathVariable long id) {

        return jdbcTemplate.queryForList("select \n" +
                "\tbe.\"name\" as \"Name\",\t\n" +
                "\tbe.barseq_experiment_id as \"Experiment id\",\n" +
                "\tsum(case when bgs.score_cnnls > 4 then 1 else 0 end) as \"High Scoring Genes\"\n" +
                "from barseq_experiment be \n" +
                "inner join barseq_gene_score bgs on be.barseq_experiment_id = bgs.barseq_experiment_id \n" +
                "where be.bagseq_library_id = " + id + "\n" +
                "group by be.\"name\", be.barseq_experiment_id", new HashMap<>());
    }

    // List of experiments and their max performing gene
    @CrossOrigin
    @GetMapping("/bagseq/{id}/maxperforminggene")
    public List<Map<String, Object>> getBagSeqMaxGene(@PathVariable long id) {

        return jdbcTemplate.queryForList("with \n" +
                "max_scores as (\n" +
                "\tselect \n" +
                "\t\ts.barseq_experiment_id,\n" +
                "\t\tmax(s.score_cnnls) max_score\n" +
                "\tfrom \n" +
                "\t\tbarseq_gene_score s\n" +
                "\tinner join barseq_experiment e using (barseq_experiment_id) \n" +
                "\twhere e.bagseq_library_id = " + id + "\n" +
                "\tgroup by s.barseq_experiment_id \n" +
                "),\n" +
                "max_scores_gene as (\n" +
                "\tselect \n" +
                "\t\tgs.barseq_experiment_id \n" +
                "\t\t,gs.score_cnnls \n" +
                "\t\t,string_agg(gene_name, ',') gene_names\n" +
                "\tfrom barseq_gene_score gs\n" +
                "\tinner join max_scores ms on gs.barseq_experiment_id = ms.barseq_experiment_id\n" +
                "\tand gs.score_cnnls = ms.max_score\n" +
                "\tgroup by \n" +
                "\t\tgs.barseq_experiment_id,\n" +
                "\t\tgs.score_cnnls\n" +
                ")\n" +
                "select \n" +
                "\tm.gene_names as \"Gene name\",\n" +
                "\tm.score_cnnls as \"Score cnnls\",\n" +
                "\te.name as \"Condition\"\n" +
                "from max_scores_gene m\n" +
                "inner join barseq_experiment e on m.barseq_experiment_id = e.barseq_experiment_id\n" +
                "order by m.score_cnnls desc", new HashMap<>());
    }


    /* * * * * * * * * * * * * * * * * * * * * * * * * * * *
     * endpoints for landing page of all expriments.
     * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    // see all experiments
    @CrossOrigin
    @GetMapping("/experiments/{id}/")
    public List<Map<String, Object>> getExperimentsLanding(@PathVariable long id) {

        return jdbcTemplate.queryForList("select \n" +
                "\tbe.name,\n" +
                "\tbe.type\n" +
                "from barseq_experiment be", new HashMap<>());
    }

    //
    @CrossOrigin
    @GetMapping("/experiments/conditions")
    public List<Map<String, Object>> getExperimentAndMaxGene() {

        return jdbcTemplate.queryForList("with \n" +
                "max_scores as (\n" +
                "\tselect \n" +
                "\t\tbarseq_experiment_id,\n" +
                "\t\tmax(score_cnnls) max_score\n" +
                "\tfrom \n" +
                "\t\tbarseq_gene_score\n" +
                "\tgroup by barseq_experiment_id \n" +
                "),\n" +
                "max_scores_gene as (\n" +
                "\tselect \n" +
                "\t\tgs.barseq_experiment_id \n" +
                "\t\t,gs.score_cnnls \n" +
                "\t\t,string_agg(gene_name, ',') gene_names\n" +
                "\tfrom barseq_gene_score gs\n" +
                "\tinner join max_scores ms on gs.barseq_experiment_id = ms.barseq_experiment_id\n" +
                "\tand gs.score_cnnls = ms.max_score\n" +
                "\tgroup by \n" +
                "\t\tgs.barseq_experiment_id,\n" +
                "\t\tgs.score_cnnls\n" +
                ")\n" +
                "select \n" +
                "\te.name,\n" +
                "\tm.score_cnnls,\n" +
                "\tm.gene_names\n" +
                "from max_scores_gene m\n" +
                "inner join barseq_experiment e on m.barseq_experiment_id = e.barseq_experiment_id;", new HashMap<>());
    }


    @CrossOrigin
    @GetMapping("/experiments")
    public List<Map<String, Object>> getExperiments(
            @RequestParam(required = false) String type
    ) {

        String QUERY = "select * from barseq_experiment";

        if (type != null) {
            QUERY = QUERY + " where type='" + type + "'";
        }

        return jdbcTemplate.queryForList(QUERY, new HashMap<String, Object>());
    }

//    @CrossOrigin
//    @GetMapping("/experiments")
//    public Collection<BarseqExperiment> getExperiments() throws IOException {
//
//        return experimentsCollector.composeExperiments();
//    }

    @CrossOrigin
    @GetMapping("/genes")
    public Collection<Gene> getGenesList() throws IOException {

        return genesDao.getGeneList();
//        return genesCollector.composeGene();
    }

    @CrossOrigin
    @GetMapping("/layout")
    public Collection<LayoutRecord> getLayoutList() throws IOException {
        return layoutCollector.composeLayout();
    }


}
