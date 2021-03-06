// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.openmrs.analytics;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.openmrs.module.atomfeed.client.AtomFeedClient;
import org.openmrs.module.atomfeed.client.AtomFeedClientFactory;

public class FeedConsumer {
	
	private List<AtomFeedClient> feedClients = new ArrayList<>();
	
	FeedConsumer(String feedBaseUrl, String jSessionId, String gcpFhirStore) throws URISyntaxException {
		// TODO what we really need is a list of pairs!
		Map<String, Class> categories = new LinkedHashMap<>();
		categories.put("Patient", Patient.class);
		categories.put("Encounter", Encounter.class);
		categories.put("Observation", Observation.class);
		// TODO add other FHIR resources that are implemented in OpenMRS.
		for (Map.Entry<String, Class> entry : categories.entrySet()) {
			AtomFeedClient feedClient = AtomFeedClientFactory.createClient(
			    new FhirEventWorker(feedBaseUrl, jSessionId, entry.getKey(), entry.getValue(), gcpFhirStore));
			// TODO check if this can be set by configuring above factory call & finalize the feed number.
			URI feedUri = new URI(feedBaseUrl + "/ws/atomfeed/" + entry.getKey().toLowerCase() + "/1");
			feedClient.setUri(feedUri);
			feedClients.add(feedClient);
		}
	}
	
	public void listen() {
		for (AtomFeedClient client : feedClients) {
			client.process();
		}
	}
	
}
