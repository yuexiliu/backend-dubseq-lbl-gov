import React from 'react';
import classes from './Footer.module.css';

const Footer = () => (
	<footer className={classes.footer}>
		<div className='container py-1'>
			<div className='row'>
				<div className='col-sm-4'>
					<p>this is the left portion of the footer</p>
				</div>
				<div className='col-sm-4'>
					<p>this is the middle portion of the footer</p>
				</div>
				<div className='col-sm-4'>
					<p>this is the right portion of the footer</p>
					<small className="d-block mb-3 text-muted">© 2021</small>
				</div>
			</div>
		</div>
	</footer>
)

export default Footer;