import React, { Component } from "react";
import { BrowserRouter as Router, Route } from "react-router-dom";
import GraphPage   from './components/Pages/Graphs/GraphsPage';
import HomePage    from './components/Pages/Home/HomePage';
import About       from './components/Pages/About/AboutPage';
import TablePage   from './components/Pages/Table/TablePage';
import SearchPage  from './components/Pages/Search/SearchPage';
import LandingPage from './components/Pages/Landing/LandingPage';
import TestingPage from './components/Pages/Testing/TestingPage';
import GenomePage  from './components/Pages/Genome/GenomeList';
import GenomeLandingPage from './components/Pages/Genome/GenomeLandingPage';
import ConditionPage from './components/Pages/Condition/ConditionPage';
import GenePage    from './components/Pages/Gene/GenePage';


class App extends Component {
	render() {
		return (
			<Router>
					<Route exact path ='/' component={HomePage}/>
					<Route path ='/graphPage' component={GraphPage}/>
					<Route path ='/about' component={About}/>
					<Route path='/listPage/:id' component={TablePage}/>
					<Route path='/search' component={SearchPage}/>
					<Route path='/landingPage/:id' component={LandingPage} />
					<Route path='/testing' component={TestingPage} />
					<Route path='/genome' component={GenomePage} />
					<Route path='/genomeLandingPage' component={GenomeLandingPage} />
					<Route path='/condition' component={ConditionPage} />
					<Route path='/gene' component={GenePage} />
			</Router>
		);
	}
}

export default App;
