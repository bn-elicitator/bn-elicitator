package bn.elicitator.collate

import bn.elicitator.Relationship
import bn.elicitator.Variable
import bn.elicitator.analysis.CandidateArc
import com.datascience.core.base.AssignedLabel
import com.datascience.core.base.LObject
import com.datascience.core.base.Worker
import com.datascience.core.nominal.CategoryValue
import com.datascience.core.nominal.INominalData
import com.datascience.core.results.DatumResult
import com.datascience.core.results.ResultsFactory
import com.datascience.core.results.WorkerResult
import com.datascience.datastoring.datamodels.memory.InMemoryNominalData
import com.datascience.datastoring.datamodels.memory.InMemoryResults
import com.datascience.gal.AbstractDawidSkene
import com.datascience.gal.BatchDawidSkene
import com.datascience.utils.CostMatrix

class DawidSkene extends CollationAlgorithm {

    private double prior
    private AbstractDawidSkene dawidSkene

    public DawidSkene( double prior, Collection<Relationship> toCollate ) {
        super( toCollate )
        this.prior = prior
    }

    public def getLabelResults() {
        dawidSkene.results.getDatumResults( dawidSkene.data.objects )
    }

    public def getWorkerResults() {
        dawidSkene.results.getWorkerResults( dawidSkene.data.workers )
    }
    
    public Collection<CandidateArc> getResultingArcs() {
        labelResults.findAll { it.value.categoryProbabilites[ "yes" ] > 0.5 }.collect {
            objectToArc( it.key )
        }
    }

    protected void collateArcs() {

        dawidSkene = new BatchDawidSkene( data : setupData() )

        def workerResultsFactory = new ResultsFactory.WorkerResultNominalFactory()
        workerResultsFactory.categories = [ "yes", "no" ]

        dawidSkene.setResults(
            new InMemoryResults<String, DatumResult, WorkerResult>(
                new ResultsFactory.DatumResultFactory(),
                workerResultsFactory
            )
        )

        dawidSkene.compute()
    }

    private INominalData setupData() {

        INominalData data = new InMemoryNominalData()

        data.initialize(
            [ "yes", "no", ],
            [ new CategoryValue( "yes", prior ), new CategoryValue( "no", 1 - prior ) ],
            new CostMatrix<String>()
        )

        toCollate.collect { relationshipToObject( it ) }.unique().each {
            data.addObject( new LObject( it ) )
        }

        toCollate.collect { it.createdBy.id }.unique().each {
            data.addWorker( new Worker( String.valueOf( it ) ) )
        }

        toCollate.each { Relationship relationship ->

            LObject object = data.getObject( relationshipToObject( relationship ) )
            Worker  worker = data.getWorker( String.valueOf( relationship.createdBy.id ) )

            data.addAssign(
                new AssignedLabel<String>(
                    worker,
                    object,
                    relationship.exists ? "yes" : "no"
                )
            )
        }

        return data

    }

    private CandidateArc objectToArc( LObject<String> object ) {
        String[] parts = object.getName().split( '-' )
        CandidateArc.getOrCreate(
            Variable.get( parts[ 0 ] as Long ),
            Variable.get( parts[ 1 ] as Long )
        )
    }

    private String relationshipToObject( Relationship relationship ) {
        "$relationship.parent.id-$relationship.child.id"
    }

}
