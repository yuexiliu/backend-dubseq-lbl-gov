import React, { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom';
import axios from 'axios'
import TableReact from '../../UI/Table/TableReact';
import Header from '../../UI/Header/Header';
import Content from '../../../hoc/Content/Content';
import Aux from '../../../hoc/Aux';
import Title from '../../UI/Title/Title';
import TableHorizontal from '../../UI/Table/TableHorizontal';
import Footer from '../../UI/Footer/Footer';
import { Link } from 'react-router-dom';

function GeneLandingPage() {

	const { id } = useParams()
	const [genes, setGenes] = useState([])
	const [stats, setStats] = useState(null)
	const [experiments, setExperiments] = useState([])
	const [fragmenExperiments, setFragmentExperiments] = useState([])
	const [geneCoverage, setGeneCoverage] = useState([])

	useEffect(() => {

		let fetchData = async () => {

			let res1 = await axios(`/api/getGenes/${id}`)
			setStats(res1.data)

			let res2 = await axios(`/api/getTopGeneExperiments/${id}`)
			res2 = addLink(res2.data, 'name', ['barseq_experiment_id'], '/bagseq/libraries/1/experiments/?')
			setExperiments(res2)

			let res3 = await axios(`/api/getGeneFragmentsExperiments/${id}`)
			res3 = addLink(res3.data, 'name', ['barseq_experiment_id'], '/bagseq/libraries/1/experiments/?')
			setFragmentExperiments(res3)

			let res4 = await axios(`/api//getGeneCoverage/${id}`)
			setGeneCoverage(res4.data)
		}

		fetchData()
	}, [])

	// DESTINATION STRING MUST BE FORMATED CORRECTLY 
	// 'bagseq/libraries/?/experiments/?'
	function addLink(data, destLinkCol, idSrcCol, path) {
		return data.map(e => {
			let newPath = path;
			idSrcCol.forEach(id => {
				newPath = newPath.replace("?", e[id])
			})
			e[destLinkCol] = <Link to={newPath}>{e[destLinkCol]}</Link>;
			return e;
		})
	}

	let ExperimentLabels = [
		{
			dataField: 'name',
			text: 'Condition',
			sort: true
		},
		{
			dataField: 'type',
			text: 'Type',
			sort: true
		},
		{
			dataField: 'barseq_experiment_id',
			text: 'ID',
			sort: true
		},
		{
			dataField: 'score_cnnls',
			text: 'Score',
			sort: true
		}
	]

	let FragmentExperiments = [
		{
			dataField: 'name',
			text: 'Name',
			sort: true
		},
		{
			dataField: 'barseq_experiment_id',
			text: 'ID',
			sort: true
		},
		{
			dataField: 'type',
			text: 'Type',
			sort: true
		},
		{
			dataField: 'barcode',
			text: 'Barcode',
			sort: true
		},
		{
			dataField: 'bagseq_fragment_id',
			text: 'FragID',
			sort: true
		},
		{
			dataField: 'score',
			text: 'Score',
			sort: true
		}
	]

	let GeneCoverageLabels = [
		{
			dataField: 'bagseq_fragment_id',
			text: 'ID',
			sort: true
		},
		{
			dataField: 'barcode',
			text: 'Barcode',
			sort: true
		},
		{
			dataField: 'score',
			text: 'Score',
			sort: true
		}
	]


	return (
		<Aux>
			<Header title='Genes' />
			<Content>
				<div className='container'>
					{stats && <Title title='Gene' specific={stats[0]['name']} />}
					{stats && <TableHorizontal content={stats} title="General Information" />}
					<br />
					<TableReact title="GeneCoverate" keyField="bagseq_fragment_id" content={geneCoverage} labels={GeneCoverageLabels} />
					<br />
					<TableReact title="Experiments" keyField="name" content={experiments} labels={ExperimentLabels} />
					<br />
					<TableReact title="FragmentExperiments" keyField="name" content={fragmenExperiments} labels={FragmentExperiments} />
					<br />
				</div>
			</Content>
			<Footer />
		</Aux>
	)
}

export default GeneLandingPage;