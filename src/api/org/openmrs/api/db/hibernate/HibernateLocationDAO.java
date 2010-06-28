/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.api.db.hibernate;

import java.util.Collections;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.MatchMode;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.LocationDAO;
import org.openmrs.util.MetadataComparator;

/**
 * Hibernate location-related database functions
 */
public class HibernateLocationDAO implements LocationDAO {
	
	private SessionFactory sessionFactory;
	
	/**
	 * @see org.openmrs.api.db.LocationDAO#setSessionFactory(org.hibernate.SessionFactory)
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * @see org.openmrs.api.db.LocationDAO#saveLocation(org.openmrs.Location)
	 */
	public Location saveLocation(Location location) {
		if (location.getChildLocations() != null && location.getLocationId() != null) {
			// hibernate has a problem updating child collections
			// if the parent object was already saved so we do it
			// explicitly here
			for (Location child : location.getChildLocations())
				if (child.getLocationId() == null)
					saveLocation(child);
		}
		
		sessionFactory.getCurrentSession().saveOrUpdate(location);
		return location;
	}
	
	/**
	 * @see org.openmrs.api.db.LocationDAO#getLocation(java.lang.Integer)
	 */
	public Location getLocation(Integer locationId) {
		return (Location) sessionFactory.getCurrentSession().get(Location.class, locationId);
	}
	
	/**
	 * @see org.openmrs.api.db.LocationDAO#getLocation(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public Location getLocation(String name) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Location.class);
		HibernateUtil.addEqCriterionForLocalizedColumn(name, "localizedName", criteria);
		
		List<Location> locations = criteria.list();
		if (null == locations || locations.isEmpty()) {
			return null;
		}
		return locations.get(0);
	}
	
	/**
	 * @see org.openmrs.api.db.LocationDAO#getAllLocations(boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<Location> getAllLocations(boolean includeRetired) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Location.class);
		if (!includeRetired) {
			criteria.add(Expression.like("retired", false));
		}
		List<Location> locations = criteria.list();
		Collections.sort(locations, new MetadataComparator(Context.getLocale()));
		return locations;
	}
	
	/**
	 * @see org.openmrs.api.db.LocationDAO#getLocations(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public List<Location> getLocations(String search) {
		if (search == null || search.equals(""))
			return getAllLocations(true);
		
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(Location.class);
		HibernateUtil.addLikeCriterionForLocalizedColumn(search, "localizedName", crit, false, MatchMode.START);
		List<Location> locations = crit.list();
		Collections.sort(locations, new MetadataComparator(Context.getLocale()));
		return locations;
	}
	
	/**
	 * @see org.openmrs.api.db.LocationDAO#deleteLocation(org.openmrs.Location)
	 */
	public void deleteLocation(Location location) {
		sessionFactory.getCurrentSession().delete(location);
	}
	
	/**
	 * @see org.openmrs.api.db.LocationDAO#saveLocation(org.openmrs.Location)
	 */
	public LocationTag saveLocationTag(LocationTag tag) {
		sessionFactory.getCurrentSession().saveOrUpdate(tag);
		return tag;
	}
	
	/**
	 * @see org.openmrs.api.db.LocationDAO#getLocationTag(java.lang.Integer)
	 */
	public LocationTag getLocationTag(Integer locationTagId) {
		return (LocationTag) sessionFactory.getCurrentSession().get(LocationTag.class, locationTagId);
	}
	
	/**
	 * @see org.openmrs.api.db.LocationDAO#getLocationTagByName(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public LocationTag getLocationTagByName(String tag) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(LocationTag.class);
		HibernateUtil.addEqCriterionForLocalizedColumn(tag, "localizedName", criteria);
		
		List<LocationTag> tags = criteria.list();
		if (null == tags || tags.isEmpty()) {
			return null;
		}
		return tags.get(0);
	}
	
	/**
	 * @see org.openmrs.api.db.LocationDAO#getAllLocationTags(boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<LocationTag> getAllLocationTags(boolean includeRetired) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(LocationTag.class);
		if (!includeRetired) {
			criteria.add(Expression.like("retired", false));
		}
		List<LocationTag> tags = criteria.list();
		Collections.sort(tags, new MetadataComparator(Context.getLocale()));
		return tags;
	}
	
	/**
	 * @see org.openmrs.api.db.LocationDAO#getLocations(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public List<LocationTag> getLocationTags(String search) {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(LocationTag.class);
		HibernateUtil.addLikeCriterionForLocalizedColumn(search, "localizedName", crit, false, MatchMode.START);
		List<LocationTag> tags = crit.list();
		Collections.sort(tags, new MetadataComparator(Context.getLocale()));
		return tags;
	}
	
	/**
	 * @see org.openmrs.api.db.LocationDAO#deleteLocationTag(org.openmrs.LocationTag)
	 */
	public void deleteLocationTag(LocationTag tag) {
		sessionFactory.getCurrentSession().delete(tag);
	}
	
	/**
	 * @see org.openmrs.api.db.LocationDAO#getLocationByUuid(java.lang.String)
	 */
	public Location getLocationByUuid(String uuid) {
		return (Location) sessionFactory.getCurrentSession().createQuery("from Location l where l.uuid = :uuid").setString(
		    "uuid", uuid).uniqueResult();
	}

	/**
     * @see org.openmrs.api.db.LocationDAO#getLocationTagByUuid(java.lang.String)
     */
    @Override
    public LocationTag getLocationTagByUuid(String uuid) {
    	return (LocationTag) sessionFactory.getCurrentSession()
    		.createQuery("from LocationTag where uuid = :uuid")
    		.setString("uuid", uuid)
    		.uniqueResult();
    }
	
}
