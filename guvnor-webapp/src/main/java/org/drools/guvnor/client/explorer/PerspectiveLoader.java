/*
 * Copyright 2011 JBoss Inc
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.drools.guvnor.client.explorer;

import org.drools.guvnor.client.common.GenericCallback;
import org.drools.guvnor.client.rpc.ConfigurationServiceAsync;
import org.drools.guvnor.client.rpc.IFramePerspectiveConfiguration;

import java.util.ArrayList;
import java.util.Collection;

public class PerspectiveLoader {

    private ConfigurationServiceAsync configurationService;

    public PerspectiveLoader(ConfigurationServiceAsync configurationService) {
        this.configurationService = configurationService;
    }

    public void loadPerspectives(final LoadPerspectives loadPerspectives) {
        configurationService.loadPerspectiveConfigurations(new GenericCallback<Collection<IFramePerspectiveConfiguration>>() {
            public void onSuccess(Collection<IFramePerspectiveConfiguration> perspectivesConfigurations) {
                handleResult(perspectivesConfigurations, loadPerspectives);
            }
        });
    }

    protected void handleResult(Collection<IFramePerspectiveConfiguration> perspectivesConfigurations, LoadPerspectives loadPerspectives) {
        Collection<Perspective> perspectives = new ArrayList<Perspective>();

        perspectives.add(getDefault());

        for (IFramePerspectiveConfiguration perspectivesConfiguration : perspectivesConfigurations) {
            perspectives.add(createIFramePerspective(perspectivesConfiguration));
        }

        loadPerspectives.loadPerspectives(perspectives);
    }

    private IFramePerspective createIFramePerspective(IFramePerspectiveConfiguration perspectivesConfiguration) {
        IFramePerspective iFramePerspective = new IFramePerspective();
        iFramePerspective.setName(perspectivesConfiguration.getName());
        iFramePerspective.setUrl(perspectivesConfiguration.getUrl());
        return iFramePerspective;
    }

    private AuthorPerspective getDefault() {
        return new AuthorPerspective();
    }

}
